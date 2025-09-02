package com.qodein.feature.promocode.submission

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.Service
import java.time.LocalDate

sealed interface ServiceSelectionUiState {
    data object Default : ServiceSelectionUiState
    data object ManualEntry : ServiceSelectionUiState
    data class Searching(val query: String, val results: List<Service>) : ServiceSelectionUiState
}

enum class PromoCodeType {
    PERCENTAGE,
    FIXED_AMOUNT
}

data class SubmissionWizardData(
    // Service Details
    val serviceName: String = "",

    // Promo Code Details
    val promoCode: String = "",
    val promoCodeType: PromoCodeType? = null,
    val discountPercentage: String = "",
    val discountAmount: String = "",
    val minimumOrderAmount: String = "",

    // Options
    val isFirstUserOnly: Boolean = false,
    val description: String = "",

    // Date Settings
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null
) {
    val hasValidService: Boolean get() = serviceName.isNotBlank()

    val hasValidPromoCode: Boolean get() = promoCode.isNotBlank()

    val hasValidDiscount: Boolean get() = when (promoCodeType) {
        PromoCodeType.PERCENTAGE -> discountPercentage.isNotBlank()
        PromoCodeType.FIXED_AMOUNT -> discountAmount.isNotBlank()
        null -> false
    }

    val hasValidMinimumOrder: Boolean get() = minimumOrderAmount.isNotBlank()

    fun canProceedFromProgressiveStep(step: ProgressiveStep): Boolean = step.canProceed(this)
}

sealed interface SubmissionWizardUiState {
    data object Loading : SubmissionWizardUiState

    data class Success(
        val wizardData: SubmissionWizardData,
        val isSubmitting: Boolean = false,
        val validationErrors: Map<String, String> = emptyMap(),
        // Progressive step state
        val currentProgressiveStep: ProgressiveStep = ProgressiveStep.SERVICE,
        // UI state
        val serviceSelectionUiState: ServiceSelectionUiState = ServiceSelectionUiState.Default,
        val showServiceSelector: Boolean = false
    ) : SubmissionWizardUiState {
        // Progressive step methods
        val canGoNextProgressive: Boolean get() = wizardData.canProceedFromProgressiveStep(currentProgressiveStep) && !isSubmitting
        val canGoPreviousProgressive: Boolean get() = !currentProgressiveStep.isFirst && !isSubmitting
        val canSubmitProgressive: Boolean get() = currentProgressiveStep.isLast &&
            wizardData.canProceedFromProgressiveStep(currentProgressiveStep) &&
            !isSubmitting
    }

    data class Error(
        val errorType: ErrorType,
        val isRetryable: Boolean,
        val shouldShowSnackbar: Boolean = true,
        val errorCode: String? = null
    ) : SubmissionWizardUiState
}
