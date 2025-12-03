package com.qodein.feature.promocode.submission

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.ui.auth.IdTokenProvider
import com.qodein.core.ui.state.UiAuthState
import com.qodein.feature.promocode.submission.validation.SubmissionField
import com.qodein.feature.promocode.submission.validation.ValidationState
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.coordinator.ServiceSelectionCoordinator
import com.qodein.shared.domain.service.selection.SearchStatus
import com.qodein.shared.domain.service.selection.SelectionState
import com.qodein.shared.domain.service.selection.ServiceSelectionAction
import com.qodein.shared.domain.service.selection.ServiceSelectionState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeRequest
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.PromocodeCode
import com.qodein.shared.model.ServiceRef
import com.qodein.shared.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
 * - Form validation and Promocode creation (UI → Domain mapping)
 * - Delegates business logic and analytics to SubmitPromocodeUseCase
 *
 * Architecture:
 * - ViewModel: UI state, navigation, form data → Promocode mapping
 * - SubmitPromocodeUseCase: Business logic, validation, persistence, analytics
 * - Clean dependency flow: UI → ViewModel → Use Case → Repository
 */
@HiltViewModel
class PromocodeSubmissionViewModel @Inject constructor(
    private val submitPromocodeUseCase: SubmitPromocodeUseCase,
    private val serviceSelectionCoordinator: ServiceSelectionCoordinator,
    private val analyticsHelper: AnalyticsHelper,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val idTokenProvider: IdTokenProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<PromocodeSubmissionUiState>(PromocodeSubmissionUiState.Loading)
    val uiState: StateFlow<PromocodeSubmissionUiState> = _uiState.asStateFlow()

    private val _authState = MutableStateFlow<UiAuthState>(UiAuthState.Uninitialized)
    val authState: StateFlow<UiAuthState> = _authState.asStateFlow()

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
            PromocodeSubmissionAction.NextProgressiveStep -> goToNextProgressiveStep()
            PromocodeSubmissionAction.PreviousProgressiveStep -> goToPreviousProgressiveStep()
            is PromocodeSubmissionAction.NavigateToStep -> navigateToStep(action.step)

            PromocodeSubmissionAction.ShowServiceSelector -> showServiceSelector()
            PromocodeSubmissionAction.HideServiceSelector -> hideServiceSelector()
            PromocodeSubmissionAction.ToggleManualEntry -> toggleManualEntry()

            is PromocodeSubmissionAction.UpdateServiceName -> updateWizardData { it.copy(serviceName = action.serviceName) }
            is PromocodeSubmissionAction.UpdateServiceUrl -> updateWizardData { it.copy(serviceUrl = action.serviceUrl) }
            is PromocodeSubmissionAction.UpdatePromocodeType -> updateWizardData { it.copy(promocodeType = action.type) }
            is PromocodeSubmissionAction.UpdatePromocode -> updatePromocode(action.promocode)
            is PromocodeSubmissionAction.UpdateDiscountPercentage -> updateWizardData { it.copy(discountPercentage = action.percentage) }
            is PromocodeSubmissionAction.UpdateDiscountAmount -> updateWizardData { it.copy(discountAmount = action.amount) }
            is PromocodeSubmissionAction.UpdateMinimumOrderAmount -> updateWizardData { it.copy(minimumOrderAmount = action.amount) }
            is PromocodeSubmissionAction.UpdateFirstUserOnly -> updateWizardData { it.copy(isFirstUserOnly = action.isFirstUserOnly) }
            is PromocodeSubmissionAction.UpdateOneTimeUseOnly -> updateWizardData { it.copy(isOneTimeUseOnly = action.isOneTimeUseOnly) }
            is PromocodeSubmissionAction.UpdateDescription -> updateWizardData { it.copy(description = action.description) }
            is PromocodeSubmissionAction.UpdateStartDate -> updateWizardData { it.copy(startDate = action.date) }
            is PromocodeSubmissionAction.UpdateEndDate -> updateWizardData { it.copy(endDate = action.date) }

            is PromocodeSubmissionAction.SubmitPromoCodeWithUser -> submitPromoCode(action.user)
            PromocodeSubmissionAction.SubmitPromoCode -> submitPromoCode()

            is PromocodeSubmissionAction.SignInWithGoogle -> signInWithGoogle(action.context)
            PromocodeSubmissionAction.DismissAuthSheet -> handleBack()

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

    // MARK: - Service Selection

    private fun showServiceSelector() {
        updateSuccessState { it.copy(showServiceSelector = true) }
        // Setup service selection when showing the selector
        setupServiceSelection()
    }

    private fun hideServiceSelector() {
        updateSuccessState { it.copy(showServiceSelector = false) }
    }

    private fun toggleManualEntry() {
        updateWizardData { data ->
            data.copy(
                isManualServiceEntry = !data.isManualServiceEntry,
                // Clear the other service selection method when toggling
                selectedService = if (!data.isManualServiceEntry) null else data.selectedService,
                serviceName = if (data.isManualServiceEntry) "" else data.serviceName,
                serviceUrl = if (data.isManualServiceEntry) "" else data.serviceUrl,
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
                            currentState.copy(wizardFlow = currentState.wizardFlow.moveToNext())
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
                            currentState.copy(wizardFlow = currentState.wizardFlow.moveToPrevious())
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
                    currentState.copy(wizardFlow = currentState.wizardFlow.moveToStep(targetStep))
                }
                else -> currentState
            }
        }
    }

    // MARK: - Authentication

    private fun setupAuthStateMonitoring() {
        viewModelScope.launch {
            getAuthStateUseCase()
                .collect { authState ->
                    val newAuthState =
                        when (authState) {
                            is AuthState.Authenticated -> UiAuthState.Authenticated(authState.userId)
                            AuthState.Unauthenticated -> UiAuthState.Unauthenticated
                        }
                    _authState.update { newAuthState }
                }
        }
    }

    private fun signInWithGoogle(context: Context) {
        Logger.i(TAG) { "signInWithGoogle() called" }

        _authState.update { UiAuthState.SigningIn }

        viewModelScope.launch {
            when (val tokenResult = idTokenProvider.getIdToken(context)) {
                is Result.Success -> {
                    when (val signInResult = signInWithGoogleUseCase(tokenResult.data)) {
                        is Result.Success -> {
                            Logger.i(TAG) { "Sign-in successful" }
                            // Auth state will be updated via setupAuthStateMonitoring
                        }
                        is Result.Error -> {
                            Logger.w(TAG) { "Sign-in failed: ${signInResult.error}" }
                            _authState.update { UiAuthState.Unauthenticated }
                            _events.emit(PromocodeSubmissionEvent.ShowError(signInResult.error))
                        }
                    }
                }
                is Result.Error -> {
                    Logger.w(TAG) { "Failed to get ID token: ${tokenResult.error}" }
                    _authState.update { UiAuthState.Unauthenticated }
                    _events.emit(PromocodeSubmissionEvent.ShowError(tokenResult.error))
                }
            }
        }
    }

    private fun clearValidationErrors() {
        updateSuccessState {
            it.copy(
                validation = ValidationState.valid(),
            )
        }
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
            is ServiceSelectionAction.ToggleService -> {
                // Get service from domain state (popular or search results)
                val allServices = (
                    newState.popular.services +
                        (newState.search.status as? SearchStatus.Success)?.services.orEmpty()
                    ).associateBy { it.id }

                val service = allServices[action.id]
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

    private fun updatePromocode(rawCode: String) {
        val validationResult = PromocodeCode.create(rawCode)
        updateWizardData { it.copy(code = rawCode) }

        val error = if (rawCode.isBlank()) {
            null
        } else {
            (validationResult as? Result.Error)?.error
        }
        updateValidationField(SubmissionField.PROMO_CODE, error)
    }

    /**
     * Submit promo code using authenticated user from current state
     */
    private fun submitPromoCode() {
        Logger.i(TAG) { "submitPromoCode() called - using authenticated user" }

        val authenticatedUser = (authState.value as? UiAuthState.Authenticated)?.userId
        if (authenticatedUser == null) {
            Logger.w(TAG) { "Cannot submit: user is not authenticated" }
            return
        }

        viewModelScope.launch {
            when (val userResult = getUserByIdUseCase(authenticatedUser.value)) {
                is Result.Success -> performSubmission(userResult.data)
                is Result.Error -> {
                    Logger.w(TAG) { "Cannot submit: failed to load user profile: ${userResult.error}" }
                    _events.emit(PromocodeSubmissionEvent.ShowError(userResult.error))
                }
            }
        }
    }

    /**
     * Core submission logic - creates Promocode and delegates to use case
     */
    private fun performSubmission(user: User) {
        val currentState = _uiState.value as? PromocodeSubmissionUiState.Success ?: return

        if (!currentState.canSubmit) {
            Logger.w(TAG) { "Cannot submit: validation failed" }
            return
        }

        val wizardData = currentState.wizardFlow.wizardData
        Logger.d(TAG) { "Submitting: service='${wizardData.effectiveServiceName}', code='${wizardData.code}'" }

        updateSuccessState { current ->
            current.copy(submission = PromocodeSubmissionState.Submitting)
        }

        viewModelScope.launch {
            val request = buildSubmitRequest(wizardData, user) ?: run {
                updateSuccessState { current ->
                    current.copy(submission = PromocodeSubmissionState.Error(PromocodeError.CreationFailure.InvalidPromocodeId))
                }
                return@launch
            }

            when (val result = submitPromocodeUseCase(request)) {
                is Result.Success -> {
                    viewModelScope.launch {
                        _events.emit(PromocodeSubmissionEvent.PromoCodeSubmitted)
                        _events.emit(PromocodeSubmissionEvent.NavigateBack)
                    }
                    updateSuccessState { current ->
                        current.copy(submission = PromocodeSubmissionState.Success(request.code))
                    }
                }
                is Result.Error -> {
                    updateSuccessState { current ->
                        current.copy(submission = PromocodeSubmissionState.Error(result.error))
                    }
                }
            }
        }
    }

    private fun buildSubmitRequest(
        wizardData: SubmissionWizardData,
        user: User
    ): SubmitPromocodeRequest? {
        val discount = when (wizardData.promocodeType) {
            PromocodeType.PERCENTAGE -> Discount.Percentage(wizardData.discountPercentage.toDoubleOrNull() ?: 0.0)
            PromocodeType.FIXED_AMOUNT -> Discount.FixedAmount(wizardData.discountAmount.toDoubleOrNull() ?: 0.0)
            null -> return null
        }

        val serviceRef = wizardData.selectedService?.id?.let { ServiceRef.ById(it) }
            ?: ServiceRef.ByName(wizardData.effectiveServiceName, wizardData.effectiveServiceUrl)

        val minimumOrder = wizardData.minimumOrderAmount.toDoubleOrNull() ?: return null
        val endDate = wizardData.endDate ?: return null

        return SubmitPromocodeRequest(
            code = wizardData.code,
            service = serviceRef,
            currentUser = user,
            discount = discount,
            minimumOrderAmount = minimumOrder,
            startDate = wizardData.startDate.toInstant(),
            endDate = endDate.toInstant(),
            description = wizardData.description.takeIf { it.isNotBlank() },
            isFirstUserOnly = wizardData.isFirstUserOnly,
            isOneTimeUseOnly = wizardData.isOneTimeUseOnly,
            isVerified = false,
        )
    }

    /**
     * Convert LocalDate to Instant for Promocode creation
     */
    private fun LocalDate.toInstant() = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant()

    // MARK: - State Update Helpers

    /**
     * Helper function to update PromocodeSubmissionUiState.Success state safely.
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
        updateSuccessState { state ->
            val newData = update(state.wizardFlow.wizardData)
            state.copy(wizardFlow = state.wizardFlow.updateData(newData))
        }
    }

    private fun updateValidationField(
        field: SubmissionField,
        error: PromocodeError.CreationFailure?
    ) {
        updateSuccessState { successState ->
            val updatedValidation = successState.validation.withFieldError(field, error)
            successState.copy(validation = updatedValidation)
        }
    }
}
