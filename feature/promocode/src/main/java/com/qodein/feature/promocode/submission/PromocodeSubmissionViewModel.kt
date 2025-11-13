package com.qodein.feature.promocode.submission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.coordinator.ServiceSelectionCoordinator
import com.qodein.shared.domain.service.selection.SelectionState
import com.qodein.shared.domain.service.selection.ServiceSelectionAction
import com.qodein.shared.domain.service.selection.ServiceSelectionState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.Service
import com.qodein.shared.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.time.toKotlinInstant

/**
 * ViewModel for the progressive promo code submission wizard.
 *
 * Manages the multi-step submission flow with clean separation of concerns:
 * - UI state management and wizard navigation
 * - Authentication state and service selection
 * - Form validation and PromoCode creation (UI → Domain mapping)
 * - Delegates business logic and analytics to SubmitPromocodeUseCase
 *
 * Architecture:
 * - ViewModel: UI state, navigation, form data → PromoCode mapping
 * - SubmitPromocodeUseCase: Business logic, validation, persistence, analytics
 * - Clean dependency flow: UI → ViewModel → Use Case → Repository
 */
@HiltViewModel
class PromocodeSubmissionViewModel @Inject constructor(
    private val submitPromocodeUseCase: SubmitPromocodeUseCase,
    private val serviceSelectionCoordinator: ServiceSelectionCoordinator,
    private val analyticsHelper: AnalyticsHelper,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PromocodeSubmissionUiState>(PromocodeSubmissionUiState.Loading)
    val uiState: StateFlow<PromocodeSubmissionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PromocodeSubmissionEvent>()
    val events = _events.asSharedFlow()

    companion object {
        private const val TAG = "PromocodeSubmissionViewModel"

        // UI Analytics constants (step navigation only)
        private const val EVENT_TYPE_PROGRESSIVE_STEP_NAVIGATION = "progressive_step_navigation"
        private const val PARAM_STEP_FROM = "step_from"
        private const val PARAM_STEP_TO = "step_to"
        private const val PARAM_DIRECTION = "direction"
        private const val DIRECTION_NEXT = "next"
        private const val DIRECTION_PREVIOUS = "previous"
    }

    init {
        initialize()
        setupAuthStateMonitoring()
    }

    // MARK: - Public API

    fun onAction(action: PromocodeSubmissionAction) {
        when (action) {
            // Progressive step navigation
            PromocodeSubmissionAction.NextProgressiveStep -> goToNextProgressiveStep()
            PromocodeSubmissionAction.PreviousProgressiveStep -> goToPreviousProgressiveStep()
            is PromocodeSubmissionAction.NavigateToStep -> navigateToStep(action.step)

            // Service selection UI actions
            PromocodeSubmissionAction.ShowServiceSelector -> showServiceSelector()
            PromocodeSubmissionAction.HideServiceSelector -> hideServiceSelector()
            PromocodeSubmissionAction.ToggleManualEntry -> toggleManualEntry()

            // Step 1: Core Details
            is PromocodeSubmissionAction.UpdateServiceName -> updateServiceName(action.serviceName)
            is PromocodeSubmissionAction.UpdatePromoCodeType -> updatePromoCodeType(action.type)
            is PromocodeSubmissionAction.UpdatePromoCode -> updatePromoCode(action.promoCode)
            is PromocodeSubmissionAction.UpdateDiscountPercentage -> updateDiscountPercentage(action.percentage)
            is PromocodeSubmissionAction.UpdateDiscountAmount -> updateDiscountAmount(action.amount)
            is PromocodeSubmissionAction.UpdateMinimumOrderAmount -> updateMinimumOrderAmount(action.amount)
            is PromocodeSubmissionAction.UpdateFirstUserOnly -> updateFirstUserOnly(action.isFirstUserOnly)
            is PromocodeSubmissionAction.UpdateOneTimeUseOnly -> updateOneTimeUseOnly(action.isOneTimeUseOnly)
            is PromocodeSubmissionAction.UpdateDescription -> updateDescription(action.description)

            // Step 2: Date Settings
            is PromocodeSubmissionAction.UpdateStartDate -> updateStartDate(action.date)
            is PromocodeSubmissionAction.UpdateEndDate -> updateEndDate(action.date)

            // Submission
            is PromocodeSubmissionAction.SubmitPromoCodeWithUser -> submitPromoCode(action.user)
            PromocodeSubmissionAction.SubmitPromoCode -> submitPromoCode()

            // Authentication
            PromocodeSubmissionAction.SignInWithGoogle -> signInWithGoogle()
            PromocodeSubmissionAction.DismissAuthSheet -> handleBack()

            // Error handling
            PromocodeSubmissionAction.RetryClicked -> initialize()
            PromocodeSubmissionAction.ClearValidationErrors -> clearValidationErrors()
        }
    }

    // MARK: - Initialization

    private fun initialize() {
        _uiState.update { PromocodeSubmissionUiState.Success.initial() }
    }

    // Service selection state managed through ServiceSelectionManager - configured for single-selection
    private val _serviceSelectionState = MutableStateFlow(
        ServiceSelectionState(selection = SelectionState.Single()), // Submission allows only single service selection
    )
    val serviceSelectionState = _serviceSelectionState.asStateFlow()

    // Expose ServiceCache for UI components that need service lookup
    val cachedServices: StateFlow<Map<String, Service>> get() = serviceSelectionCoordinator.cachedServices

    // MARK: - Service Selection

    private fun showServiceSelector() {
        updateSuccessState { it.showServiceSelector() }
        // Setup service selection when showing the selector
        setupServiceSelection()
    }

    private fun hideServiceSelector() {
        updateSuccessState { it.hideServiceSelector() }
    }

    private fun toggleManualEntry() {
        updateWizardData { data ->
            data.copy(
                isManualServiceEntry = !data.isManualServiceEntry,
                // Clear the other service selection method when toggling
                selectedService = if (!data.isManualServiceEntry) null else data.selectedService,
                serviceName = if (data.isManualServiceEntry) "" else data.serviceName,
            )
        }
    }

    // MARK: - Navigation

    private fun goToNextProgressiveStep() {
        _uiState.update { currentState ->
            when (currentState) {
                is PromocodeSubmissionUiState.Success -> {
                    if (currentState.canGoNext) {
                        val currentStep = currentState.wizardFlow.currentStep
                        val nextStep = currentStep.next()
                        if (nextStep != null) {
                            // Track progressive step navigation
                            analyticsHelper.logEvent(
                                AnalyticsEvent(
                                    type = EVENT_TYPE_PROGRESSIVE_STEP_NAVIGATION,
                                    extras = listOf(
                                        AnalyticsEvent.Param(PARAM_STEP_FROM, currentStep.name),
                                        AnalyticsEvent.Param(PARAM_STEP_TO, nextStep.name),
                                        AnalyticsEvent.Param(PARAM_DIRECTION, DIRECTION_NEXT),
                                    ),
                                ),
                            )
                            currentState.moveToNextStep()
                        } else {
                            currentState
                        }
                    } else {
                        currentState
                    }
                }
                else -> currentState
            }
        }
    }

    private fun goToPreviousProgressiveStep() {
        _uiState.update { currentState ->
            when (currentState) {
                is PromocodeSubmissionUiState.Success -> {
                    if (currentState.canGoPrevious) {
                        val currentStep = currentState.wizardFlow.currentStep
                        val previousStep = currentStep.previous()
                        if (previousStep != null) {
                            // Track progressive step navigation
                            analyticsHelper.logEvent(
                                AnalyticsEvent(
                                    type = EVENT_TYPE_PROGRESSIVE_STEP_NAVIGATION,
                                    extras = listOf(
                                        AnalyticsEvent.Param(PARAM_STEP_FROM, currentStep.name),
                                        AnalyticsEvent.Param(PARAM_STEP_TO, previousStep.name),
                                        AnalyticsEvent.Param(PARAM_DIRECTION, DIRECTION_PREVIOUS),
                                    ),
                                ),
                            )
                            currentState.moveToPreviousStep()
                        } else {
                            currentState
                        }
                    } else {
                        currentState
                    }
                }
                else -> currentState
            }
        }
    }

    private fun navigateToStep(targetStep: PromocodeSubmissionStep) {
        _uiState.update { currentState ->
            when (currentState) {
                is PromocodeSubmissionUiState.Success -> {
                    // Track step navigation
                    analyticsHelper.logEvent(
                        AnalyticsEvent(
                            type = EVENT_TYPE_PROGRESSIVE_STEP_NAVIGATION,
                            extras = listOf(
                                AnalyticsEvent.Param(PARAM_STEP_FROM, currentState.wizardFlow.currentStep.name),
                                AnalyticsEvent.Param(PARAM_STEP_TO, targetStep.name),
                                AnalyticsEvent.Param(PARAM_DIRECTION, "DIRECT"),
                            ),
                        ),
                    )
                    currentState.copy(
                        wizardFlow = currentState.wizardFlow.copy(
                            currentStep = targetStep,
                        ),
                    )
                }
                else -> currentState
            }
        }
    }

    // MARK: - Authentication

    private fun setupAuthStateMonitoring() {
        viewModelScope.launch {
            getAuthStateUseCase()
                .collect { user ->
                    _uiState.update { currentState ->
                        when (currentState) {
                            is PromocodeSubmissionUiState.Success -> {
                                val newAuthState = if (user != null) {
                                    PromocodeSubmissionAuthenticationState.Authenticated(user)
                                } else {
                                    PromocodeSubmissionAuthenticationState.Unauthenticated
                                }
                                currentState.updateAuthentication(newAuthState)
                            }
                            is PromocodeSubmissionUiState.Loading,
                            is PromocodeSubmissionUiState.Error -> currentState
                        }
                    }
                }
        }
    }

    private fun signInWithGoogle() {
        Logger.i(TAG) { "signInWithGoogle() called" }

        _uiState.update { currentState ->
            when (currentState) {
                is PromocodeSubmissionUiState.Success -> currentState.updateAuthentication(PromocodeSubmissionAuthenticationState.Loading)
                else -> currentState
            }
        }

        viewModelScope.launch {
            signInWithGoogleUseCase()
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            Logger.i(TAG) { "Sign-in successful" }
                            // Auth state will be updated via setupAuthStateMonitoring
                        }
                        is Result.Error -> {
                            Logger.w(TAG) { "Sign-in failed: ${result.error}" }
                            // Reset to unauthenticated
                            _uiState.update { currentState ->
                                when (currentState) {
                                    is PromocodeSubmissionUiState.Success ->
                                        currentState.updateAuthentication(PromocodeSubmissionAuthenticationState.Unauthenticated)
                                    else -> currentState
                                }
                            }
                            _events.emit(PromocodeSubmissionEvent.ShowError(result.error))
                        }
                    }
                }
        }
    }

    // MARK: - Data Updates (Step 1: Core Details)

    private fun updateServiceName(serviceName: String) {
        // Update the manual service name field
        updateWizardData { it.copy(serviceName = serviceName) }
    }

    private fun updatePromoCodeType(type: PromoCodeType) {
        updateWizardData { it.copy(promoCodeType = type) }
    }

    private fun updatePromoCode(promoCode: String) {
        updateWizardData { it.copy(promoCode = promoCode) }
    }

    private fun updateDiscountPercentage(percentage: String) {
        updateWizardData { it.copy(discountPercentage = percentage) }
    }

    private fun updateDiscountAmount(amount: String) {
        updateWizardData { it.copy(discountAmount = amount) }
    }

    private fun updateMinimumOrderAmount(amount: String) {
        updateWizardData { it.copy(minimumOrderAmount = amount) }
    }

    private fun updateFirstUserOnly(isFirstUserOnly: Boolean) {
        updateWizardData { it.copy(isFirstUserOnly = isFirstUserOnly) }
    }

    private fun updateOneTimeUseOnly(isOneTimeUseOnly: Boolean) {
        updateWizardData { it.copy(isOneTimeUseOnly = isOneTimeUseOnly) }
    }

    // MARK: - Data Updates (Step 2: Date Settings)
    private fun updateStartDate(date: LocalDate) {
        updateWizardData { it.copy(startDate = date) }
    }

    private fun updateEndDate(date: LocalDate) {
        updateWizardData { it.copy(endDate = date) }
    }

    private fun updateDescription(description: String) {
        updateWizardData { it.copy(description = description) }
    }

    private fun clearValidationErrors() {
        updateSuccessState { it.clearValidationErrors() }
    }

    // MARK: - Service Selection Management

    private fun setupServiceSelection() {
        serviceSelectionCoordinator.setupServiceSelection(
            scope = viewModelScope,
            getCurrentState = { _serviceSelectionState.value },
            onStateUpdate = { newState ->
                _serviceSelectionState.update { newState }
            },
        )
    }

    fun onServiceSelectionAction(action: ServiceSelectionAction) {
        val currentState = _serviceSelectionState.value
        val newState = serviceSelectionCoordinator.handleAction(currentState, action)

        _serviceSelectionState.update { newState }

        when (action) {
            is ServiceSelectionAction.SelectService -> {
                val service = serviceSelectionCoordinator.cachedServices.value[action.id.value]
                service?.let {
                    updateWizardData { wizardData -> wizardData.copy(selectedService = it) }
                    onAction(PromocodeSubmissionAction.HideServiceSelector)
                }
            }
            else -> { /* Other actions don't need additional handling */ }
        }
    }

    private fun handleBack() {
        viewModelScope.launch {
            _events.emit(PromocodeSubmissionEvent.NavigateBack)
        }
    }

    // MARK: - Submission Logic

    /**
     * Submit promo code with explicit user (for external calls)
     */
    private fun submitPromoCode(user: User) {
        Logger.i(TAG) { "submitPromoCode() called with user: ${user.id.value}" }
        performSubmission(user)
    }

    /**
     * Submit promo code using authenticated user from current state
     */
    private fun submitPromoCode() {
        Logger.i(TAG) { "submitPromoCode() called - using authenticated user" }

        val currentState = _uiState.value as? PromocodeSubmissionUiState.Success ?: run {
            Logger.w(TAG) { "Cannot submit: currentState is not Success" }
            return
        }

        val authenticatedUser = (currentState.authentication as? PromocodeSubmissionAuthenticationState.Authenticated)?.user
        if (authenticatedUser == null) {
            Logger.w(TAG) { "Cannot submit: user is not authenticated" }
            return
        }

        performSubmission(authenticatedUser)
    }

    /**
     * Core submission logic - creates PromoCode and delegates to use case
     */
    private fun performSubmission(user: User) {
        val currentState = _uiState.value as? PromocodeSubmissionUiState.Success ?: return

        if (!currentState.canSubmit) {
            Logger.w(TAG) { "Cannot submit: validation failed" }
            return
        }

        val wizardData = currentState.wizardFlow.wizardData
        Logger.d(TAG) { "Submitting: service='${wizardData.effectiveServiceName}', code='${wizardData.promoCode}'" }

        updateSuccessState { it.startSubmission() }

        viewModelScope.launch {
            val promoCodeResult = createPromoCodeFromWizardData(wizardData, user)

            when (promoCodeResult) {
                is Result.Success -> {
                    submitPromocodeUseCase(promoCodeResult.data)
                        .catch { exception ->
                            updateSuccessState { it.submitError(exception) }
                        }
                        .collect { result ->
                            updateSuccessState { state ->
                                when (result) {
                                    is Result.Success -> {
                                        // UI navigation events (not business analytics)
                                        viewModelScope.launch {
                                            _events.emit(PromocodeSubmissionEvent.PromoCodeSubmitted)
                                            _events.emit(PromocodeSubmissionEvent.NavigateBack)
                                        }
                                        state.submitSuccess(result.data.id.value)
                                    }
                                    is Result.Error -> state.submitError(Exception(result.error.toString()))
                                }
                            }
                        }
                }
                is Result.Error -> {
                    updateSuccessState { it.submitError(Exception(promoCodeResult.error.toString())) }
                }
            }
        }
    }

    /**
     * Create PromoCode instance from wizard data and user info
     * (UI → Domain mapping responsibility)
     */
    private fun createPromoCodeFromWizardData(
        wizardData: SubmissionWizardData,
        user: User
    ): Result<PromoCode, SystemError> {
        val serviceLogoUrl = wizardData.selectedService?.logoUrl

        return try {
            when (wizardData.promoCodeType) {
                PromoCodeType.PERCENTAGE -> Result.Success(
                    PromoCode.create(
                        code = wizardData.promoCode,
                        serviceName = wizardData.effectiveServiceName,
                        discount = Discount.Percentage(wizardData.discountPercentage.toDoubleOrNull() ?: 0.0),
                        serviceId = wizardData.selectedService?.id,
                        description = wizardData.description.takeIf { it.isNotBlank() },
                        minimumOrderAmount = wizardData.minimumOrderAmount.toDoubleOrNull() ?: 0.0,
                        startDate = wizardData.startDate.toInstant(),
                        endDate = wizardData.endDate!!.toInstant(),
                        isFirstUserOnly = wizardData.isFirstUserOnly,
                        createdBy = user.id,
                        createdByUsername = user.profile.displayName,
                        createdByAvatarUrl = user.profile.photoUrl,
                        serviceLogoUrl = serviceLogoUrl,
                        targetCountries = listOf("KZ"), // Kazakhstan market
                    ).getOrThrow(),
                )

                PromoCodeType.FIXED_AMOUNT -> Result.Success(
                    PromoCode.create(
                        code = wizardData.promoCode,
                        serviceName = wizardData.effectiveServiceName,
                        discount = Discount.FixedAmount(wizardData.discountAmount.toDoubleOrNull() ?: 0.0),
                        serviceId = wizardData.selectedService?.id,
                        description = wizardData.description.takeIf { it.isNotBlank() },
                        minimumOrderAmount = wizardData.minimumOrderAmount.toDoubleOrNull() ?: 0.0,
                        startDate = wizardData.startDate.toInstant(),
                        endDate = wizardData.endDate!!.toInstant(),
                        isFirstUserOnly = wizardData.isFirstUserOnly,
                        createdBy = user.id,
                        createdByUsername = user.profile.displayName,
                        createdByAvatarUrl = user.profile.photoUrl,
                        serviceLogoUrl = serviceLogoUrl,
                        targetCountries = listOf("KZ"), // Kazakhstan market
                    ).getOrThrow(),
                )

                null -> Result.Error(SystemError.Unknown)
            }
        } catch (exception: Exception) {
            Result.Error(SystemError.Unknown)
        }
    }

    /**
     * Convert LocalDate to Instant for PromoCode creation
     */
    private fun LocalDate.toInstant() = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant()

    // MARK: - State Update Helpers

    /**
     * Helper function to update PromocodeSubmissionUiState.Success state safely.
     * Uses the extension functions from StateUpdateExtensions.kt for ergonomic updates.
     */
    private inline fun updateSuccessState(crossinline update: (PromocodeSubmissionUiState.Success) -> PromocodeSubmissionUiState.Success) {
        _uiState.update { currentState ->
            when (currentState) {
                is PromocodeSubmissionUiState.Success -> update(currentState)
                else -> currentState
            }
        }
    }

    /**
     * Helper function specifically for wizard data updates using extensions
     */
    private fun updateWizardData(update: (SubmissionWizardData) -> SubmissionWizardData) {
        updateSuccessState { it.updateWizardData(update) }
    }
}
