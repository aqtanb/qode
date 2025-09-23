package com.qodein.feature.promocode.submission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.shared.common.result.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.manager.ServiceSearchManager
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
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
class SubmissionWizardViewModel @Inject constructor(
    private val submitPromocodeUseCase: SubmitPromocodeUseCase,
    private val serviceSearchManager: ServiceSearchManager,
    private val analyticsHelper: AnalyticsHelper,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubmissionWizardUiState>(SubmissionWizardUiState.Loading)
    val uiState: StateFlow<SubmissionWizardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SubmissionWizardEvent>()
    val events = _events.asSharedFlow()

    companion object {
        private const val TAG = "SubmissionWizardViewModel"

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

    fun onAction(action: SubmissionWizardAction) {
        when (action) {
            // Progressive step navigation
            SubmissionWizardAction.NextProgressiveStep -> goToNextProgressiveStep()
            SubmissionWizardAction.PreviousProgressiveStep -> goToPreviousProgressiveStep()
            is SubmissionWizardAction.NavigateToStep -> navigateToStep(action.step)

            // Service selection UI actions
            SubmissionWizardAction.ShowServiceSelector -> showServiceSelector()
            SubmissionWizardAction.HideServiceSelector -> hideServiceSelector()
            SubmissionWizardAction.ToggleManualEntry -> toggleManualEntry()

            // Step 1: Core Details
            is SubmissionWizardAction.SelectService -> selectService(action.service)
            is SubmissionWizardAction.UpdateServiceName -> updateServiceName(action.serviceName)
            is SubmissionWizardAction.UpdatePromoCodeType -> updatePromoCodeType(action.type)
            is SubmissionWizardAction.SearchServices -> searchServices(action.query)
            is SubmissionWizardAction.UpdatePromoCode -> updatePromoCode(action.promoCode)
            is SubmissionWizardAction.UpdateDiscountPercentage -> updateDiscountPercentage(action.percentage)
            is SubmissionWizardAction.UpdateDiscountAmount -> updateDiscountAmount(action.amount)
            is SubmissionWizardAction.UpdateMinimumOrderAmount -> updateMinimumOrderAmount(action.amount)
            is SubmissionWizardAction.UpdateFirstUserOnly -> updateFirstUserOnly(action.isFirstUserOnly)
            is SubmissionWizardAction.UpdateOneTimeUseOnly -> updateOneTimeUseOnly(action.isOneTimeUseOnly)
            is SubmissionWizardAction.UpdateDescription -> updateDescription(action.description)

            // Step 2: Date Settings
            is SubmissionWizardAction.UpdateStartDate -> updateStartDate(action.date)
            is SubmissionWizardAction.UpdateEndDate -> updateEndDate(action.date)

            // Submission
            is SubmissionWizardAction.SubmitPromoCodeWithUser -> submitPromoCode(action.user)
            SubmissionWizardAction.SubmitPromoCode -> submitPromoCode()

            // Authentication
            SubmissionWizardAction.SignInWithGoogle -> signInWithGoogle()
            SubmissionWizardAction.DismissAuthSheet -> handleBack()
            SubmissionWizardAction.ClearAuthError -> clearAuthError()

            // Error handling
            SubmissionWizardAction.RetryClicked -> initialize()
            SubmissionWizardAction.ClearValidationErrors -> clearValidationErrors()
        }
    }

    // MARK: - Initialization

    private fun initialize() {
        _uiState.update { SubmissionWizardUiState.Success.initial() }
    }

    private val _searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<Service>>(emptyList())
    private val _isSearching = MutableStateFlow(false)

    val searchQuery = _searchQuery.asStateFlow()
    val searchResults = _searchResults.asStateFlow()
    val isSearching = _isSearching.asStateFlow()

    // MARK: - Service Selection

    private fun showServiceSelector() {
        updateSuccessState { it.showServiceSelector() }
        // Activate service search and set up the search flow
        serviceSearchManager.activate()
        serviceSearchManager.clearQuery()
        // Trigger empty query to load popular services immediately
        serviceSearchManager.updateQuery("")
        setupServiceSearch()
    }

    private fun setupServiceSearch() {
        viewModelScope.launch {
            combine(
                serviceSearchManager.searchQuery,
                serviceSearchManager.searchResult,
            ) { query, result ->
                _searchQuery.value = query
                when (result) {
                    is Result.Loading -> {
                        _isSearching.value = true
                        _searchResults.value = emptyList()
                    }
                    is Result.Success -> {
                        _isSearching.value = false
                        _searchResults.value = result.data
                    }
                    is Result.Error -> {
                        _isSearching.value = false
                        _searchResults.value = emptyList()
                    }
                }
            }.collect()
        }
    }

    private fun hideServiceSelector() {
        updateSuccessState { it.hideServiceSelector() }
    }

    private fun toggleManualEntry() {
        // TODO: Implement manual entry toggle with new architecture
    }

    // MARK: - Navigation

    private fun goToNextProgressiveStep() {
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> {
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
                is SubmissionWizardUiState.Success -> {
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

    private fun navigateToStep(targetStep: SubmissionStep) {
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> {
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
                .collect { authResult ->
                    _uiState.update { currentState ->
                        when (currentState) {
                            is SubmissionWizardUiState.Success -> {
                                val newAuthState = when (authResult) {
                                    is Result.Loading -> AuthenticationState.Loading
                                    is Result.Success -> {
                                        when (val authState = authResult.data) {
                                            is AuthState.Loading -> AuthenticationState.Loading
                                            is AuthState.Authenticated -> AuthenticationState.Authenticated(authState.user)
                                            is AuthState.Unauthenticated -> AuthenticationState.Unauthenticated
                                        }
                                    }
                                    is Result.Error -> AuthenticationState.Error(authResult.exception)
                                }
                                currentState.updateAuthentication(newAuthState)
                            }
                            is SubmissionWizardUiState.Loading,
                            is SubmissionWizardUiState.Error -> currentState
                        }
                    }
                }
        }
    }

    private fun signInWithGoogle() {
        Logger.i(TAG) { "signInWithGoogle() called" }

        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> currentState.updateAuthentication(AuthenticationState.Loading)
                else -> currentState
            }
        }

        viewModelScope.launch {
            signInWithGoogleUseCase()
                .collect { result ->
                    _uiState.update { currentState ->
                        when (currentState) {
                            is SubmissionWizardUiState.Success -> {
                                val newAuthState = when (result) {
                                    is Result.Loading -> AuthenticationState.Loading
                                    is Result.Success -> {
                                        Logger.i(TAG) { "Sign-in successful" }
                                        // Auth state will be updated via setupAuthStateMonitoring
                                        return@update currentState
                                    }
                                    is Result.Error -> {
                                        Logger.w(TAG) { "Sign-in failed: ${result.exception.message}" }
                                        AuthenticationState.Error(result.exception)
                                    }
                                }
                                currentState.updateAuthentication(newAuthState)
                            }
                            else -> currentState
                        }
                    }
                }
        }
    }

    private fun clearAuthError() {
        Logger.i(TAG) { "clearAuthError() called" }
        updateSuccessState { currentState ->
            when (currentState.authentication) {
                is AuthenticationState.Error -> currentState.updateAuthentication(AuthenticationState.Unauthenticated)
                else -> currentState
            }
        }
    }

    // MARK: - Data Updates (Step 1: Core Details)

    private fun selectService(service: Service) {
        updateWizardData { it.copy(selectedService = service) }
    }

    private fun updateServiceName(serviceName: String) {
        // Update the manual service name field
        updateWizardData { it.copy(serviceName = serviceName) }
    }

    private fun updatePromoCodeType(type: PromoCodeType) {
        updateWizardData { it.copy(promoCodeType = type) }
    }

    private fun searchServices(query: String) {
        serviceSearchManager.updateQuery(query)
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

    private fun handleBack() {
        viewModelScope.launch {
            _events.emit(SubmissionWizardEvent.NavigateBack)
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

        val currentState = _uiState.value as? SubmissionWizardUiState.Success ?: run {
            Logger.w(TAG) { "Cannot submit: currentState is not Success" }
            return
        }

        val authenticatedUser = (currentState.authentication as? AuthenticationState.Authenticated)?.user
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
        val currentState = _uiState.value as? SubmissionWizardUiState.Success ?: return

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
                                    is Result.Loading -> state // Already in submitting state
                                    is Result.Success -> {
                                        // UI navigation events (not business analytics)
                                        viewModelScope.launch {
                                            _events.emit(SubmissionWizardEvent.PromoCodeSubmitted)
                                            _events.emit(SubmissionWizardEvent.NavigateBack)
                                        }
                                        state.submitSuccess(result.data.id.value)
                                    }
                                    is Result.Error -> state.submitError(result.exception)
                                }
                            }
                        }
                }
                is Result.Error -> {
                    updateSuccessState { it.submitError(promoCodeResult.exception) }
                }
                is Result.Loading -> {
                    // Should not happen for synchronous validation
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
    ): Result<PromoCode> {
        val serviceLogoUrl = wizardData.selectedService?.logoUrl
        val category = wizardData.selectedService?.category ?: "Unspecified"

        return try {
            when (wizardData.promoCodeType) {
                PromoCodeType.PERCENTAGE -> Result.Success(
                    PromoCode.createPercentage(
                        code = wizardData.promoCode,
                        serviceName = wizardData.effectiveServiceName,
                        serviceId = wizardData.selectedService?.id,
                        discountPercentage = wizardData.discountPercentage.toDoubleOrNull() ?: 0.0,
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
                        category = category,
                    ).getOrThrow(),
                )

                PromoCodeType.FIXED_AMOUNT -> Result.Success(
                    PromoCode.createFixedAmount(
                        code = wizardData.promoCode,
                        serviceName = wizardData.effectiveServiceName,
                        serviceId = wizardData.selectedService?.id,
                        discountAmount = wizardData.discountAmount.toDoubleOrNull() ?: 0.0,
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
                        category = category,
                    ).getOrThrow(),
                )

                null -> Result.Error(IllegalStateException("PromoCode type must be specified"))
            }
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }

    /**
     * Convert LocalDate to Instant for PromoCode creation
     */
    private fun LocalDate.toInstant() = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant()

    // MARK: - State Update Helpers

    /**
     * Helper function to update SubmissionWizardUiState.Success state safely.
     * Uses the extension functions from StateUpdateExtensions.kt for ergonomic updates.
     */
    private inline fun updateSuccessState(crossinline update: (SubmissionWizardUiState.Success) -> SubmissionWizardUiState.Success) {
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> update(currentState)
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
