package com.qodein.feature.promocode.submission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logPromoCodeSubmission
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.getErrorCode
import com.qodein.shared.common.result.isRetryable
import com.qodein.shared.common.result.shouldShowSnackbar
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.usecase.promocode.CreatePromoCodeUseCase
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.model.PromoCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.toKotlinInstant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SubmissionWizardViewModel @Inject constructor(
    private val createPromoCodeUseCase: CreatePromoCodeUseCase,
    private val searchServicesUseCase: SearchServicesUseCase,
    private val getPopularServicesUseCase: GetPopularServicesUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubmissionWizardUiState>(SubmissionWizardUiState.Loading)
    val uiState: StateFlow<SubmissionWizardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SubmissionWizardEvent>()
    val events = _events.asSharedFlow()

    // Service search state
    private val searchQueryFlow = MutableStateFlow("")

    init {
        initialize()
        setupServiceSearch()
    }

    fun onAction(action: SubmissionWizardAction) {
        when (action) {
            // Navigation
            SubmissionWizardAction.GoToNextStep -> goToNextStep()
            SubmissionWizardAction.GoToPreviousStep -> goToPreviousStep()
            is SubmissionWizardAction.GoToStep -> goToStep(action.step)

            // Step 1: Service & Type
            is SubmissionWizardAction.UpdateServiceName -> updateServiceName(action.serviceName)
            is SubmissionWizardAction.UpdatePromoCodeType -> updatePromoCodeType(action.type)
            is SubmissionWizardAction.SearchServices -> searchServices(action.query)

            // Step 2: Type Details
            is SubmissionWizardAction.UpdatePromoCode -> updatePromoCode(action.promoCode)
            is SubmissionWizardAction.UpdateDiscountPercentage -> updateDiscountPercentage(action.percentage)
            is SubmissionWizardAction.UpdateDiscountAmount -> updateDiscountAmount(action.amount)
            is SubmissionWizardAction.UpdateMinimumOrderAmount -> updateMinimumOrderAmount(action.amount)
            is SubmissionWizardAction.UpdateFirstUserOnly -> updateFirstUserOnly(action.isFirstUserOnly)

            // Step 3: Date Settings
            is SubmissionWizardAction.UpdateStartDate -> updateStartDate(action.date)
            is SubmissionWizardAction.UpdateEndDate -> updateEndDate(action.date)

            // Step 4: Optional Details
            is SubmissionWizardAction.UpdateTitle -> updateTitle(action.title)
            is SubmissionWizardAction.UpdateDescription -> updateDescription(action.description)
            is SubmissionWizardAction.UpdateScreenshotUrl -> updateScreenshotUrl(action.url)

            // Submission
            SubmissionWizardAction.SubmitPromoCode -> submitPromoCode()

            // Error handling
            SubmissionWizardAction.RetryClicked -> initialize()
            SubmissionWizardAction.ClearValidationErrors -> clearValidationErrors()
        }
    }

    private fun initialize() {
        _uiState.value = SubmissionWizardUiState.Success(
            currentStep = SubmissionWizardStep.SERVICE_AND_TYPE,
            wizardData = SubmissionWizardData(),
        )

        // Load popular services initially
        loadPopularServices()
    }

    private fun setupServiceSearch() {
        searchQueryFlow
            .debounce(300) // Debounce for 300ms
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .onEach { query ->
                performServiceSearch(query)
            }
            .launchIn(viewModelScope)
    }

    private fun loadPopularServices() {
        viewModelScope.launch {
            updateServiceLoadingState(true)

            getPopularServicesUseCase()
                .catch { exception ->
                    // Silently fail for popular services, continue with empty list
                    updateServiceLoadingState(false)
                }
                .collect { result ->
                    when (result) {
                        is Result.Loading -> { /* Loading already handled above */ }
                        is Result.Success -> {
                            updateAvailableServices(result.data)
                            updateServiceLoadingState(false)
                        }
                        is Result.Error -> {
                            // Silently fail for popular services
                            updateServiceLoadingState(false)
                        }
                    }
                }
        }
    }

    private fun performServiceSearch(query: String) {
        viewModelScope.launch {
            updateServiceLoadingState(true)

            searchServicesUseCase(query)
                .catch { exception ->
                    updateServiceLoadingState(false)
                }
                .collect { result ->
                    when (result) {
                        is Result.Loading -> { /* Loading already handled above */ }
                        is Result.Success -> {
                            updateAvailableServices(result.data)
                            updateServiceLoadingState(false)
                        }
                        is Result.Error -> {
                            updateServiceLoadingState(false)
                        }
                    }
                }
        }
    }

    private fun goToNextStep() {
        val currentState = getCurrentSuccessState() ?: return
        val nextStep = currentState.currentStep.next() ?: return

        if (currentState.canGoNext) {
            // Auto-fill title when moving to OPTIONAL_DETAILS step
            val updatedData = if (nextStep == SubmissionWizardStep.OPTIONAL_DETAILS &&
                currentState.wizardData.title.isBlank()
            ) {
                currentState.wizardData.copy(
                    title = generateAutoTitle(currentState.wizardData),
                )
            } else {
                currentState.wizardData
            }

            _uiState.value = currentState.copy(
                currentStep = nextStep,
                wizardData = updatedData,
            )

            // Track step navigation
            analyticsHelper.logEvent(
                com.qodein.core.analytics.AnalyticsEvent(
                    type = "wizard_step_navigation",
                    extras = listOf(
                        com.qodein.core.analytics.AnalyticsEvent.Param("step_from", currentState.currentStep.name),
                        com.qodein.core.analytics.AnalyticsEvent.Param("step_to", nextStep.name),
                        com.qodein.core.analytics.AnalyticsEvent.Param("direction", "next"),
                    ),
                ),
            )
        }
    }

    private fun goToPreviousStep() {
        val currentState = getCurrentSuccessState() ?: return
        val previousStep = currentState.currentStep.previous() ?: return

        if (currentState.canGoPrevious) {
            _uiState.value = currentState.copy(currentStep = previousStep)

            // Track step navigation
            analyticsHelper.logEvent(
                com.qodein.core.analytics.AnalyticsEvent(
                    type = "wizard_step_navigation",
                    extras = listOf(
                        com.qodein.core.analytics.AnalyticsEvent.Param("step_from", currentState.currentStep.name),
                        com.qodein.core.analytics.AnalyticsEvent.Param("step_to", previousStep.name),
                        com.qodein.core.analytics.AnalyticsEvent.Param("direction", "previous"),
                    ),
                ),
            )
        }
    }

    private fun goToStep(step: SubmissionWizardStep) {
        val currentState = getCurrentSuccessState() ?: return
        _uiState.value = currentState.copy(currentStep = step)
    }

    // Step 1 Updates
    private fun updateServiceName(serviceName: String) {
        updateWizardData { it.copy(serviceName = serviceName) }
    }

    private fun updatePromoCodeType(type: PromoCodeType) {
        updateWizardData { it.copy(promoCodeType = type) }
    }

    private fun searchServices(query: String) {
        searchQueryFlow.value = query
        updateServiceSearchQuery(query)
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

    // Step 4 Updates
    private fun updateTitle(title: String) {
        updateWizardData { it.copy(title = title) }
    }

    private fun updateDescription(description: String) {
        updateWizardData { it.copy(description = description) }
    }

    private fun updateScreenshotUrl(url: String?) {
        updateWizardData { it.copy(screenshotUrl = url) }
    }

    private fun clearValidationErrors() {
        val currentState = getCurrentSuccessState() ?: return
        _uiState.value = currentState.copy(validationErrors = emptyMap())
    }

    private fun submitPromoCode() {
        val currentState = getCurrentSuccessState() ?: return
        if (!currentState.canSubmit) return

        val wizardData = currentState.wizardData

        // Validate dates
        if (wizardData.endDate == null || !wizardData.endDate.isAfter(wizardData.startDate)) {
            _uiState.value = currentState.copy(
                validationErrors = mapOf("endDate" to "End date must be selected and after start date"),
            )
            return
        }

        _uiState.value = currentState.copy(isSubmitting = true)

        viewModelScope.launch {
            val promoCodeResult = when (wizardData.promoCodeType) {
                PromoCodeType.PERCENTAGE -> PromoCode.createPercentage(
                    code = wizardData.promoCode,
                    serviceName = wizardData.serviceName,
                    discountPercentage = wizardData.discountPercentage.toDoubleOrNull() ?: 0.0,
                    title = wizardData.title,
                    description = wizardData.description.takeIf { it.isNotBlank() },
                    minimumOrderAmount = wizardData.minimumOrderAmount.toDoubleOrNull() ?: 0.0,
                    startDate = wizardData.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    endDate = wizardData.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    isFirstUserOnly = wizardData.isFirstUserOnly,
                    screenshotUrl = wizardData.screenshotUrl,
                )
                PromoCodeType.FIXED_AMOUNT -> PromoCode.createFixedAmount(
                    code = wizardData.promoCode,
                    serviceName = wizardData.serviceName,
                    discountAmount = wizardData.discountAmount.toDoubleOrNull() ?: 0.0,
                    title = wizardData.title,
                    description = wizardData.description.takeIf { it.isNotBlank() },
                    minimumOrderAmount = wizardData.minimumOrderAmount.toDoubleOrNull() ?: 0.0,
                    startDate = wizardData.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    endDate = wizardData.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    isFirstUserOnly = wizardData.isFirstUserOnly,
                    screenshotUrl = wizardData.screenshotUrl,
                )
                null -> return@launch
            }

            promoCodeResult.fold(
                onSuccess = { promoCode ->
                    createPromoCodeUseCase(promoCode)
                        .catch { exception ->
                            _uiState.value = SubmissionWizardUiState.Error(
                                errorType = exception.toErrorType(),
                                isRetryable = exception.isRetryable(),
                                shouldShowSnackbar = exception.shouldShowSnackbar(),
                                errorCode = exception.getErrorCode(),
                            )
                        }
                        .collect { result ->
                            when (result) {
                                is Result.Loading -> { /* Loading state if needed */ }
                                is Result.Success -> {
                                    // Track successful promo code submission
                                    analyticsHelper.logPromoCodeSubmission(
                                        promocodeId = result.data.id.value,
                                        promocodeType = when (promoCode) {
                                            is PromoCode.PercentagePromoCode -> "percentage"
                                            is PromoCode.FixedAmountPromoCode -> "fixed_amount"
                                        },
                                        success = true,
                                    )
                                    _events.emit(SubmissionWizardEvent.PromoCodeSubmitted)
                                    _events.emit(SubmissionWizardEvent.NavigateBack)
                                }
                                is Result.Error -> {
                                    // Track failed promo code submission
                                    analyticsHelper.logPromoCodeSubmission(
                                        promocodeId = "unknown",
                                        promocodeType = when (promoCode) {
                                            is PromoCode.PercentagePromoCode -> "percentage"
                                            is PromoCode.FixedAmountPromoCode -> "fixed_amount"
                                        },
                                        success = false,
                                    )
                                    _uiState.value = SubmissionWizardUiState.Error(
                                        errorType = result.exception.toErrorType(),
                                        isRetryable = result.exception.isRetryable(),
                                        shouldShowSnackbar = result.exception.shouldShowSnackbar(),
                                        errorCode = result.exception.getErrorCode(),
                                    )
                                }
                            }
                        }
                },
                onFailure = { exception ->
                    _uiState.value = SubmissionWizardUiState.Error(
                        errorType = exception.toErrorType(),
                        isRetryable = exception.isRetryable(),
                        shouldShowSnackbar = exception.shouldShowSnackbar(),
                        errorCode = exception.getErrorCode(),
                    )
                },
            )
        }
    }

    private fun updateWizardData(update: (SubmissionWizardData) -> SubmissionWizardData) {
        val currentState = getCurrentSuccessState() ?: return
        val newData = update(currentState.wizardData)
        _uiState.value = currentState.copy(wizardData = newData)
    }

    // Service search helper methods
    private fun updateAvailableServices(services: List<com.qodein.shared.model.Service>) {
        val currentState = getCurrentSuccessState() ?: return
        _uiState.value = currentState.copy(availableServices = services)
    }

    private fun updateServiceLoadingState(isLoading: Boolean) {
        val currentState = getCurrentSuccessState() ?: return
        _uiState.value = currentState.copy(isLoadingServices = isLoading)
    }

    private fun updateServiceSearchQuery(query: String) {
        val currentState = getCurrentSuccessState() ?: return
        _uiState.value = currentState.copy(serviceSearchQuery = query)
    }

    private fun getCurrentSuccessState(): SubmissionWizardUiState.Success? = _uiState.value as? SubmissionWizardUiState.Success

    private fun generateAutoTitle(wizardData: SubmissionWizardData): String =
        when (wizardData.promoCodeType) {
            PromoCodeType.PERCENTAGE -> {
                val percentage = wizardData.discountPercentage.toIntOrNull() ?: 0
                "$percentage% off at ${wizardData.serviceName.takeIf { it.isNotBlank() } ?: "this service"}"
            }
            PromoCodeType.FIXED_AMOUNT -> {
                val amount = wizardData.discountAmount.toIntOrNull() ?: 0
                "₸$amount off at ${wizardData.serviceName.takeIf { it.isNotBlank() } ?: "this service"}"
            }
            null -> "Promo code for ${wizardData.serviceName.takeIf { it.isNotBlank() } ?: "this service"}"
        }
}
