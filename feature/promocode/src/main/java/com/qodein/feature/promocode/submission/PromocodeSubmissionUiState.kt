package com.qodein.feature.promocode.submission

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Service
import java.time.LocalDate

enum class PromoCodeType {
    PERCENTAGE,
    FIXED_AMOUNT
}

data class SubmissionWizardData(
    // Service Details
    val selectedService: Service? = null,
    val serviceName: String = "", // Manual service name entry
    val isManualServiceEntry: Boolean = false, // Toggle between service selector and manual entry

    // Promo Code Details
    val promoCode: String = "",
    val promoCodeType: PromoCodeType? = null,
    val discountPercentage: String = "",
    val discountAmount: String = "",
    val minimumOrderAmount: String = "",

    // Options
    val isFirstUserOnly: Boolean = false,
    val isOneTimeUseOnly: Boolean = false,
    val description: String = "",

    // Date Settings
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null
) {
    val hasValidService: Boolean get() = selectedService != null || serviceName.isNotBlank()
    val effectiveServiceName: String get() = selectedService?.name ?: serviceName

    val hasValidPromoCode: Boolean get() = promoCode.isNotBlank()

    val hasValidDiscount: Boolean get() = when (promoCodeType) {
        PromoCodeType.PERCENTAGE -> discountPercentage.isNotBlank()
        PromoCodeType.FIXED_AMOUNT -> discountAmount.isNotBlank()
        null -> false
    }

    val hasValidMinimumOrder: Boolean get() = minimumOrderAmount.isNotBlank()

    fun canProceedFromProgressiveStep(step: PromocodeSubmissionStep): Boolean = step.canProceed(this)
}

sealed interface PromocodeSubmissionUiState {
    data object Loading : PromocodeSubmissionUiState

    data class Success(
        val wizardFlow: WizardFlowState,
        val authentication: PromocodeSubmissionAuthenticationState,
        val validation: ValidationState,
        val submission: PromocodeSubmissionState,
        val showServiceSelector: Boolean = false
    ) : PromocodeSubmissionUiState {

        // Navigation capabilities
        val canGoNext: Boolean get() = wizardFlow.canGoNext && submission !is PromocodeSubmissionState.Submitting
        val canGoPrevious: Boolean get() = wizardFlow.canGoPrevious && submission !is PromocodeSubmissionState.Submitting
        val canSubmit: Boolean get() = wizardFlow.canSubmit && validation.isValid && submission !is PromocodeSubmissionState.Submitting

        companion object {
            fun initial(): Success =
                Success(
                    wizardFlow = WizardFlowState.initial(),
                    authentication = PromocodeSubmissionAuthenticationState.Loading,
                    validation = ValidationState.valid(),
                    submission = PromocodeSubmissionState.Idle,
                )
        }
    }

    data class Error(
        val errorType: OperationError,
        val isRetryable: Boolean,
        val shouldShowSnackbar: Boolean = true,
        val errorCode: String? = null
    ) : PromocodeSubmissionUiState
}
