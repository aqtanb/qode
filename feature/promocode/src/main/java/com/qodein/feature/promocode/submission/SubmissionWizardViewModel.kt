package com.qodein.feature.promocode.submission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logPromoCodeSubmission
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.getErrorCode
import com.qodein.shared.common.result.isRetryable
import com.qodein.shared.common.result.shouldShowSnackbar
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.manager.ServiceSearchManager
import com.qodein.shared.domain.usecase.promocode.CreatePromoCodeUseCase
import com.qodein.shared.model.PromoCode
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
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubmissionWizardUiState>(SubmissionWizardUiState.Loading)
    val uiState: StateFlow<SubmissionWizardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SubmissionWizardEvent>()
    val events = _events.asSharedFlow()

    companion object {
        // Analytics constants
        private const val PROMO_CODE_TYPE_PERCENTAGE = "percentage"
        private const val PROMO_CODE_TYPE_FIXED_AMOUNT = "fixed_amount"
        private const val EVENT_TYPE_PROGRESSIVE_STEP_NAVIGATION = "progressive_step_navigation"

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
    }

    // MARK: - Public API

    fun onAction(action: SubmissionWizardAction) {
        when (action) {
            // Progressive step navigation
            SubmissionWizardAction.NextProgressiveStep -> goToNextProgressiveStep()
            SubmissionWizardAction.PreviousProgressiveStep -> goToPreviousProgressiveStep()

            // Service selection UI actions
            SubmissionWizardAction.ShowServiceSelector -> showServiceSelector()
            SubmissionWizardAction.HideServiceSelector -> hideServiceSelector()
            SubmissionWizardAction.ToggleManualEntry -> toggleManualEntry()

            // Step 1: Core Details
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
            SubmissionWizardAction.SubmitPromoCode -> submitPromoCode()

            // Error handling
            SubmissionWizardAction.RetryClicked -> initialize()
            SubmissionWizardAction.ClearValidationErrors -> clearValidationErrors()
        }
    }

    // MARK: - Initialization

    private fun initialize() {
        _uiState.update {
            SubmissionWizardUiState.Success(
                wizardData = SubmissionWizardData(),
                currentProgressiveStep = ProgressiveStep.SERVICE,
            )
        }
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
                        is SubmissionWizardUiState.Success -> currentState.copy(serviceSelectionUiState = newSelectionUiState)
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
                is SubmissionWizardUiState.Success -> currentState.copy(showServiceSelector = true)
                else -> currentState
            }
        }
        serviceSearchManager.clearQuery()
    }

    private fun hideServiceSelector() {
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> currentState.copy(
                    showServiceSelector = false,
                )
                else -> currentState
            }
        }
    }

    private fun toggleManualEntry() {
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> {
                    val newState = when (currentState.serviceSelectionUiState) {
                        ServiceSelectionUiState.ManualEntry -> ServiceSelectionUiState.Default
                        else -> ServiceSelectionUiState.ManualEntry
                    }
                    currentState.copy(serviceSelectionUiState = newState)
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
                    val nextStep = currentState.currentProgressiveStep.next()
                    if (nextStep != null && currentState.canGoNextProgressive) {
                        // Track progressive step navigation
                        analyticsHelper.logEvent(
                            AnalyticsEvent(
                                type = EVENT_TYPE_PROGRESSIVE_STEP_NAVIGATION,
                                extras = listOf(
                                    AnalyticsEvent.Param(PARAM_STEP_FROM, currentState.currentProgressiveStep.name),
                                    AnalyticsEvent.Param(PARAM_STEP_TO, nextStep.name),
                                    AnalyticsEvent.Param(PARAM_DIRECTION, DIRECTION_NEXT),
                                ),
                            ),
                        )
                        currentState.copy(currentProgressiveStep = nextStep)
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
                    val previousStep = currentState.currentProgressiveStep.previous()
                    if (previousStep != null && currentState.canGoPreviousProgressive) {
                        // Track progressive step navigation
                        analyticsHelper.logEvent(
                            AnalyticsEvent(
                                type = EVENT_TYPE_PROGRESSIVE_STEP_NAVIGATION,
                                extras = listOf(
                                    AnalyticsEvent.Param(PARAM_STEP_FROM, currentState.currentProgressiveStep.name),
                                    AnalyticsEvent.Param(PARAM_STEP_TO, previousStep.name),
                                    AnalyticsEvent.Param(PARAM_DIRECTION, DIRECTION_PREVIOUS),
                                ),
                            ),
                        )
                        currentState.copy(currentProgressiveStep = previousStep)
                    } else {
                        currentState
                    }
                }
                else -> currentState
            }
        }
    }

    // MARK: - Data Updates

    private fun updateServiceName(serviceName: String) {
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
                is SubmissionWizardUiState.Success -> currentState.copy(validationErrors = emptyMap())
                else -> currentState
            }
        }
    }

    private fun submitPromoCode() {
        val currentState = _uiState.value as? SubmissionWizardUiState.Success ?: return
        if (!currentState.canSubmitProgressive) return

        val wizardData = currentState.wizardData

        _uiState.update { state ->
            when (state) {
                is SubmissionWizardUiState.Success -> state.copy(isSubmitting = true)
                else -> state
            }
        }

        viewModelScope.launch {
            val promoCodeResult = when (wizardData.promoCodeType) {
                PromoCodeType.PERCENTAGE -> PromoCode.createPercentage(
                    code = wizardData.promoCode,
                    serviceName = wizardData.serviceName,
                    discountPercentage = wizardData.discountPercentage.toDoubleOrNull() ?: 0.0,
                    description = wizardData.description.takeIf { it.isNotBlank() },
                    minimumOrderAmount = wizardData.minimumOrderAmount.toDoubleOrNull() ?: 0.0,
                    startDate = wizardData.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    endDate = wizardData.endDate!!.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    isFirstUserOnly = wizardData.isFirstUserOnly,
                )
                PromoCodeType.FIXED_AMOUNT -> PromoCode.createFixedAmount(
                    code = wizardData.promoCode,
                    serviceName = wizardData.serviceName,
                    discountAmount = wizardData.discountAmount.toDoubleOrNull() ?: 0.0,
                    description = wizardData.description.takeIf { it.isNotBlank() },
                    minimumOrderAmount = wizardData.minimumOrderAmount.toDoubleOrNull() ?: 0.0,
                    startDate = wizardData.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    endDate = wizardData.endDate!!.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                    isFirstUserOnly = wizardData.isFirstUserOnly,
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
                                is Result.Loading -> { /* Loading state if needed */ }
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
                                    _uiState.update {
                                        SubmissionWizardUiState.Error(
                                            errorType = result.exception.toErrorType(),
                                            isRetryable = result.exception.isRetryable(),
                                            shouldShowSnackbar = result.exception.shouldShowSnackbar(),
                                            errorCode = result.exception.getErrorCode(),
                                        )
                                    }
                                }
                            }
                        }
                },
                onFailure = { exception ->
                    _uiState.update {
                        SubmissionWizardUiState.Error(
                            errorType = exception.toErrorType(),
                            isRetryable = exception.isRetryable(),
                            shouldShowSnackbar = exception.shouldShowSnackbar(),
                            errorCode = exception.getErrorCode(),
                        )
                    }
                },
            )
        }
    }

    private fun updateWizardData(update: (SubmissionWizardData) -> SubmissionWizardData) {
        _uiState.update { currentState ->
            when (currentState) {
                is SubmissionWizardUiState.Success -> {
                    val newData = update(currentState.wizardData)
                    currentState.copy(wizardData = newData)
                }
                else -> currentState
            }
        }
    }
}
