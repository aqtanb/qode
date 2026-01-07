package com.qodein.feature.promocode.submission

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.refresh.RefreshTarget
import com.qodein.core.ui.refresh.ScreenRefreshCoordinator
import com.qodein.feature.promocode.submission.validation.SubmissionField
import com.qodein.feature.promocode.submission.validation.ValidationState
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeRequest
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.domain.usecase.service.GetServiceLogoUrlUseCase
import com.qodein.shared.domain.usecase.service.GetServicesByIdsUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.PromocodeCode
import com.qodein.shared.model.ServiceId
import com.qodein.shared.model.ServiceRef
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.toKotlinInstant

class PromocodeSubmissionViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val submitPromocodeUseCase: SubmitPromocodeUseCase,
    private val analyticsHelper: AnalyticsHelper,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val refreshCoordinator: ScreenRefreshCoordinator,
    private val getServicesByIdsUseCase: GetServicesByIdsUseCase,
    private val getServiceLogoUrlUseCase: GetServiceLogoUrlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PromocodeSubmissionUiState>(PromocodeSubmissionUiState.Loading)
    val uiState: StateFlow<PromocodeSubmissionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PromocodeSubmissionEvent>()
    val events = _events.asSharedFlow()

    private var currentAuthState: AuthState = AuthState.Unauthenticated
    private var pendingSubmission: Boolean = false

    companion object {
        private const val TAG = "PromocodeSubmissionViewModel"
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
        observeAuthResult()
    }

    fun onAction(action: PromocodeSubmissionAction) {
        when (action) {
            PromocodeSubmissionAction.NextProgressiveStep -> goToNextProgressiveStep()
            PromocodeSubmissionAction.PreviousProgressiveStep -> goToPreviousProgressiveStep()
            is PromocodeSubmissionAction.NavigateToStep -> navigateToStep(action.step)

            PromocodeSubmissionAction.ShowServiceSelector -> showServiceSelector()
            PromocodeSubmissionAction.HideServiceSelector -> hideServiceSelector()
            PromocodeSubmissionAction.ToggleManualEntry -> toggleManualEntry()
            PromocodeSubmissionAction.ConfirmServiceLogo -> confirmServiceLogo()
            PromocodeSubmissionAction.DismissServiceConfirmation -> dismissServiceConfirmation()

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

            PromocodeSubmissionAction.RetryClicked -> initialize()
            PromocodeSubmissionAction.ClearValidationErrors -> clearValidationErrors()
        }
    }

    private fun initialize() {
        _uiState.update { PromocodeSubmissionUiState.Success.initial() }
    }

    internal fun applyServiceSelection(serviceId: ServiceId) {
        viewModelScope.launch {
            val result = getServicesByIdsUseCase(setOf(serviceId)).firstOrNull() ?: return@launch
            updateWizardData { it.copy(selectedService = result) }
            Logger.d(TAG) { "Service selected: ${result.name}" }
        }
    }

    private fun showServiceSelector() {
        viewModelScope.launch {
            val currentState = _uiState.value as? PromocodeSubmissionUiState.Success ?: return@launch
            val currentServiceId = currentState.wizardFlow.wizardData.selectedService?.id
            _events.emit(PromocodeSubmissionEvent.ShowServiceSelection(currentServiceId))
            Logger.i(TAG) { "Service selection dialog shown" }
        }
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

    private fun goToNextProgressiveStep() {
        val currentState = _uiState.value as? PromocodeSubmissionUiState.Success ?: return

        // Special handling for SERVICE step with manual entry - validate logo first
        if (currentState.wizardFlow.currentStep == PromocodeSubmissionStep.SERVICE &&
            currentState.wizardFlow.wizardData.isManualServiceEntry &&
            currentState.canGoNext
        ) {
            validateServiceLogoAndProceed()
            return
        }

        // Normal progression for all other steps
        _uiState.update { state ->
            when (state) {
                is PromocodeSubmissionUiState.Success -> {
                    if (state.canGoNext) {
                        val currentStep = state.wizardFlow.currentStep
                        val nextStep = currentStep.next()
                        if (nextStep != null) {
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
                            state.copy(wizardFlow = state.wizardFlow.moveToNext())
                        } else {
                            state
                        }
                    } else {
                        state
                    }
                }
                else -> state
            }
        }
    }

    // TODO: Check for the service duplication
    private fun validateServiceLogoAndProceed() {
        val currentState = _uiState.value as? PromocodeSubmissionUiState.Success ?: return
        val domain = currentState.wizardFlow.wizardData.serviceUrl

        viewModelScope.launch {
            when (val result = getServiceLogoUrlUseCase(domain)) {
                is Result.Success -> {
                    // Logo found - show confirmation dialog
                    updateSuccessState { state ->
                        state.copy(
                            serviceConfirmationDialog = ServiceConfirmationDialogState(
                                serviceName = state.wizardFlow.wizardData.serviceName,
                                serviceUrl = state.wizardFlow.wizardData.serviceUrl,
                                logoUrl = result.data,
                            ),
                        )
                    }
                }
                is Result.Error -> {
                    // Logo not found or error - show error toast
                    _events.emit(PromocodeSubmissionEvent.ShowError(result.error.toUiText()))
                }
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

    private fun setupAuthStateMonitoring() {
        viewModelScope.launch {
            getAuthStateUseCase()
                .collect { authState ->
                    currentAuthState = authState
                }
        }
    }

    private fun observeAuthResult() {
        viewModelScope.launch {
            savedStateHandle.getStateFlow("auth_result", "")
                .collect { result ->
                    if (result == "success") {
                        // Execute pending submission if auth was successful
                        if (pendingSubmission) {
                            submitPromoCode()
                            pendingSubmission = false
                        }
                        // Reset the flag so it doesn't trigger again
                        savedStateHandle["auth_result"] = ""
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

    private fun handleBack() {
        viewModelScope.launch {
            _events.emit(PromocodeSubmissionEvent.NavigateBack)
        }
    }

    private fun confirmServiceLogo() {
        updateSuccessState { it.copy(serviceConfirmationDialog = null, wizardFlow = it.wizardFlow.moveToNext()) }
    }

    private fun dismissServiceConfirmation() {
        updateSuccessState { it.copy(serviceConfirmationDialog = null) }
    }

    // MARK: - Submission Logic

    /**
     * Submit promocode with explicit user (for external calls)
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
     * Submit promocode using authenticated user from current state
     */
    private fun submitPromoCode() {
        Logger.i(TAG) { "submitPromoCode() called - using authenticated user" }

        val userId = (currentAuthState as? AuthState.Authenticated)?.userId
        if (userId == null) {
            Logger.w(TAG) { "Cannot submit: user is not authenticated" }
            return
        }

        viewModelScope.launch {
            when (val userResult = getUserByIdUseCase(userId.value)) {
                is Result.Success -> performSubmission(userResult.data)
                is Result.Error -> {
                    Logger.w(TAG) { "Cannot submit: failed to load user profile: ${userResult.error}" }
                    _events.emit(PromocodeSubmissionEvent.ShowError(userResult.error.toUiText()))
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
                    refreshCoordinator.triggerRefresh(RefreshTarget.HOME)
                    viewModelScope.launch {
                        _events.emit(PromocodeSubmissionEvent.PromoCodeSubmitted)
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
