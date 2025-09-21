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
    val selectedService: Service? = null,
    val serviceName: String = "", // Manual service name entry

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
    val hasValidService: Boolean get() = selectedService != null || serviceName.isNotBlank()
    val effectiveServiceName: String get() = selectedService?.name ?: serviceName

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
        val wizardFlow: WizardFlowState,
        val authentication: AuthenticationState,
        val validation: ValidationState,
        val submission: SubmissionState,
        // Temporary UI concerns (TODO: extract service selection as separate feature)
        val serviceSelectionUiState: ServiceSelectionUiState = ServiceSelectionUiState.Default,
        val showServiceSelector: Boolean = false
    ) : SubmissionWizardUiState {

        // Navigation capabilities using new state composition
        val canGoNextProgressive: Boolean get() = wizardFlow.canGoNext && submission !is SubmissionState.Submitting
        val canGoPreviousProgressive: Boolean get() = wizardFlow.canGoPrevious && submission !is SubmissionState.Submitting
        val canSubmitProgressive: Boolean get() = wizardFlow.canSubmit && validation.isValid && submission !is SubmissionState.Submitting

        companion object {
            fun initial(): Success =
                Success(
                    wizardFlow = WizardFlowState.initial(),
                    authentication = AuthenticationState.Loading,
                    validation = ValidationState.valid(),
                    submission = SubmissionState.Idle,
                )
        }
    }

    data class Error(
        val errorType: ErrorType,
        val isRetryable: Boolean,
        val shouldShowSnackbar: Boolean = true,
        val errorCode: String? = null
    ) : SubmissionWizardUiState
}
