package com.qodein.feature.promocode.submission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logPromoCodeSubmission
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.getErrorCode
import com.qodein.shared.common.result.isRetryable
import com.qodein.shared.common.result.shouldShowSnackbar
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.manager.ServiceSearchManager
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.promocode.CreatePromoCodeUseCase
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.Service
import com.qodein.shared.model.UserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.time.toKotlinInstant

@HiltViewModel
class SubmissionWizardViewModel @Inject constructor(
    private val createPromoCodeUseCase: CreatePromoCodeUseCase,
    private val serviceSearchManager: ServiceSearchManager,
    private val analyticsHelper: AnalyticsHelper,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    /**
     * Simple user data structure for promo code creation
     */
    data class UserData(val id: UserId, val username: String?, val avatarUrl: String?)

    private val _uiState = MutableStateFlow<SubmissionWizardUiState>(SubmissionWizardUiState.Loading)
    val uiState: StateFlow<SubmissionWizardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SubmissionWizardEvent>()
    val events = _events.asSharedFlow()

    companion object {
        // Analytics constants
        private const val PROMO_CODE_TYPE_PERCENTAGE = "percentage"
        private const val PROMO_CODE_TYPE_FIXED_AMOUNT = "fixed_amount"
        private const val EVENT_TYPE_PROGRESSIVE_STEP_NAVIGATION = "progressive_step_navigation"
        private const val TAG = "SubmissionWizardViewModel"

        // Analytics parameter keys
        private const val PARAM_STEP_FROM = "step_from"
        private const val PARAM_STEP_TO = "step_to"
        private const val PARAM_DIRECTION = "direction"
        private const val DIRECTION_NEXT = "next"
        private const val DIRECTION_PREVIOUS = "previous"
    }

    init {
        initialize()
        setupServiceSearch()
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
            is SubmissionWizardAction.UpdateDescription -> updateDescription(action.description)

            // Step 2: Date Settings
            is SubmissionWizardAction.UpdateStartDate -> updateStartDate(action.date)
            is SubmissionWizardAction.UpdateEndDate -> updateEndDate(action.date)

            // Submission
            is SubmissionWizardAction.SubmitPromoCodeWithUser -> submitPromoCode(action.userData)
            SubmissionWizardAction.SubmitPromoCode -> submitPromoCodeFromViewModel()

            // Authentication
            SubmissionWizardAction.SignInWithGoogle -> signInWithGoogle()
            SubmissionWizardAction.DismissAuthSheet -> dismissAuthSheet()
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

    private fun setupServiceSearch() {
        viewModelScope.launch {
            combine(
                serviceSearchManager.searchQuery,
                serviceSearchManager.searchResult,
            ) { query, result ->
                when (result) {
                    is Result.Loading -> ServiceSelectionUiState.Searching(query, emptyList())
                    is Result.Success -> ServiceSelectionUiState.Searching(query, result.data)
                    is Result.Error -> ServiceSelectionUiState.Searching(query, emptyList())
                }
            }.collect { newSelectionUiState ->
                _uiState.update { currentState ->
                    when (currentState) {
                        is SubmissionWizardUiState.Success -> currentState.updateServiceSelection(uiState = newSelectionUiState)
                        else -> currentState
                    }
                }
            }
        }
    }

    // MARK: - Service Selection

    private fun showServiceSelector() {
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> currentState.showServiceSelector()
                else -> currentState
            }
        }
        serviceSearchManager.clearQuery()
    }

    private fun hideServiceSelector() {
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> currentState.hideServiceSelector()
                else -> currentState
            }
        }
    }

    private fun toggleManualEntry() {
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> {
                    val newUiState = when (currentState.serviceSelectionUiState) {
                        ServiceSelectionUiState.ManualEntry -> ServiceSelectionUiState.Default
                        else -> ServiceSelectionUiState.ManualEntry
                    }
                    currentState.updateServiceSelection(uiState = newUiState)
                }
                else -> currentState
            }
        }
    }

    // MARK: - Navigation

    private fun goToNextProgressiveStep() {
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> {
                    if (currentState.canGoNextProgressive) {
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
                    if (currentState.canGoPreviousProgressive) {
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

    private fun navigateToStep(targetStep: ProgressiveStep) {
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

    private fun dismissAuthSheet() {
        Logger.i(TAG) { "dismissAuthSheet() called" }
        // TODO: Add proper dismiss logic when authentication becomes optional
        // For now, user must authenticate to proceed
    }

    private fun clearAuthError() {
        Logger.i(TAG) { "clearAuthError() called" }
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> {
                    when (currentState.authentication) {
                        is AuthenticationState.Error -> currentState.updateAuthentication(AuthenticationState.Unauthenticated)
                        else -> currentState
                    }
                }
                else -> currentState
            }
        }
    }

    // MARK: - Data Updates

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

    // Step 2 Updates
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

    // Step 3 Updates
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
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> currentState.clearValidationErrors()
                else -> currentState
            }
        }
    }

    private fun submitPromoCode(userData: UserData) {
        Logger.i(TAG) { "submitPromoCode() called with user: ${userData.id.value}" }

        val currentState = _uiState.value as? SubmissionWizardUiState.Success ?: run {
            Logger.w(TAG) { "Cannot submit: currentState is not Success, actual state: ${_uiState.value}" }
            return
        }

        if (!currentState.canSubmitProgressive) {
            Logger.w(TAG) { "Cannot submit: canSubmitProgressive is false" }
            return
        }

        val wizardData = currentState.wizardFlow.wizardData
        Logger.d(TAG) { "Submitting: service='${wizardData.effectiveServiceName}', code='${wizardData.promoCode}'" }

        _uiState.update { state ->
            when (state) {
                is SubmissionWizardUiState.Success -> state.startSubmission()
                else -> state
            }
        }

        viewModelScope.launch {
            val serviceLogoUrl = wizardData.selectedService?.logoUrl

            val promoCodeResult = when (wizardData.promoCodeType) {
                PromoCodeType.PERCENTAGE -> PromoCode.createPercentage(
                    code = wizardData.promoCode,
                    serviceName = wizardData.effectiveServiceName,
                    serviceId = wizardData.selectedService?.id,
                    discountPercentage = wizardData.discountPercentage.toDoubleOrNull() ?: 0.0,
                    description = wizardData.description.takeIf { it.isNotBlank() },
                    minimumOrderAmount = wizardData.minimumOrderAmount.toDoubleOrNull() ?: 0.0,
                    startDate = wizardData.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    endDate = wizardData.endDate!!.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    isFirstUserOnly = wizardData.isFirstUserOnly,
                    createdBy = userData.id,
                    createdByUsername = userData.username,
                    createdByAvatarUrl = userData.avatarUrl,
                    serviceLogoUrl = serviceLogoUrl,
                    // Add Kazakhstan targeting for market alignment
                    targetCountries = listOf("KZ"),
                    // Set service category from selected service
                    category = wizardData.selectedService?.category ?: "Unspecified",
                )
                PromoCodeType.FIXED_AMOUNT -> PromoCode.createFixedAmount(
                    code = wizardData.promoCode,
                    serviceName = wizardData.effectiveServiceName,
                    serviceId = wizardData.selectedService?.id,
                    discountAmount = wizardData.discountAmount.toDoubleOrNull() ?: 0.0,
                    description = wizardData.description.takeIf { it.isNotBlank() },
                    minimumOrderAmount = wizardData.minimumOrderAmount.toDoubleOrNull() ?: 0.0,
                    startDate = wizardData.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    endDate = wizardData.endDate!!.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    isFirstUserOnly = wizardData.isFirstUserOnly,
                    createdBy = userData.id,
                    createdByUsername = userData.username,
                    createdByAvatarUrl = userData.avatarUrl,
                    serviceLogoUrl = serviceLogoUrl,
                    // Add Kazakhstan targeting for market alignment
                    targetCountries = listOf("KZ"),
                    // Set service category from selected service
                    category = wizardData.selectedService?.category ?: "Unspecified",
                )
                null -> return@launch
            }

            promoCodeResult.fold(
                onSuccess = { promoCode ->
                    createPromoCodeUseCase(promoCode)
                        .catch { exception ->
                            _uiState.update {
                                SubmissionWizardUiState.Error(
                                    errorType = exception.toErrorType(),
                                    isRetryable = exception.isRetryable(),
                                    shouldShowSnackbar = exception.shouldShowSnackbar(),
                                    errorCode = exception.getErrorCode(),
                                )
                            }
                        }
                        .collect { result ->
                            when (result) {
                                is Result.Loading -> { /* Already in submitting state */ }
                                is Result.Success -> {
                                    // Track successful promo code submission
                                    analyticsHelper.logPromoCodeSubmission(
                                        promocodeId = result.data.id.value,
                                        promocodeType = when (promoCode) {
                                            is PromoCode.PercentagePromoCode -> PROMO_CODE_TYPE_PERCENTAGE
                                            is PromoCode.FixedAmountPromoCode -> PROMO_CODE_TYPE_FIXED_AMOUNT
                                        },
                                        success = true,
                                    )
                                    _uiState.update { state ->
                                        when (state) {
                                            is SubmissionWizardUiState.Success -> state.submitSuccess(result.data.id.value)
                                            else -> state
                                        }
                                    }
                                    _events.emit(SubmissionWizardEvent.PromoCodeSubmitted)
                                    _events.emit(SubmissionWizardEvent.NavigateBack)
                                }
                                is Result.Error -> {
                                    // Track failed promo code submission
                                    analyticsHelper.logPromoCodeSubmission(
                                        promocodeId = "unknown",
                                        promocodeType = when (promoCode) {
                                            is PromoCode.PercentagePromoCode -> PROMO_CODE_TYPE_PERCENTAGE
                                            is PromoCode.FixedAmountPromoCode -> PROMO_CODE_TYPE_FIXED_AMOUNT
                                        },
                                        success = false,
                                    )
                                    _uiState.update { state ->
                                        when (state) {
                                            is SubmissionWizardUiState.Success -> state.submitError(result.exception)
                                            else -> SubmissionWizardUiState.Error(
                                                errorType = result.exception.toErrorType(),
                                                isRetryable = result.exception.isRetryable(),
                                                shouldShowSnackbar = result.exception.shouldShowSnackbar(),
                                                errorCode = result.exception.getErrorCode(),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                },
                onFailure = { exception ->
                    _uiState.update { state ->
                        when (state) {
                            is SubmissionWizardUiState.Success -> state.submitError(exception)
                            else -> SubmissionWizardUiState.Error(
                                errorType = exception.toErrorType(),
                                isRetryable = exception.isRetryable(),
                                shouldShowSnackbar = exception.shouldShowSnackbar(),
                                errorCode = exception.getErrorCode(),
                            )
                        }
                    }
                },
            )
        }
    }

    private fun submitPromoCodeFromViewModel() {
        Logger.i(TAG) { "submitPromoCodeFromViewModel() called" }

        val currentState = _uiState.value as? SubmissionWizardUiState.Success ?: run {
            Logger.w(TAG) { "Cannot submit: currentState is not Success, actual state: ${_uiState.value}" }
            return
        }

        val authenticatedUser = (currentState.authentication as? AuthenticationState.Authenticated)?.user
        if (authenticatedUser == null) {
            Logger.w(TAG) { "Cannot submit: user is not authenticated" }
            return
        }

        val userData = UserData(
            id = authenticatedUser.id,
            username = authenticatedUser.profile.displayName,
            avatarUrl = authenticatedUser.profile.photoUrl,
        )

        submitPromoCode(userData)
    }

    private fun updateWizardData(update: (SubmissionWizardData) -> SubmissionWizardData) {
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> currentState.updateWizardData(update)
                else -> currentState
            }
        }
    }
}
