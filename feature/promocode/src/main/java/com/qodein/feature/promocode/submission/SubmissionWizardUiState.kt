package com.qodein.feature.promocode.submission

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.Service
import java.time.LocalDate

enum class PromoCodeType {
    PERCENTAGE,
    FIXED_AMOUNT
}

data class SubmissionWizardData(
    // Step 1: Service & Type
    val serviceName: String = "",
    val promoCodeType: PromoCodeType? = null,

    // Step 2: Type Details
    val promoCode: String = "",
    val discountPercentage: String = "",
    val discountAmount: String = "",
    val minimumOrderAmount: String = "",
    val isFirstUserOnly: Boolean = false,

    // Step 3: Date Settings
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,

    // Step 4: Optional Details
    val title: String = "",
    val description: String = "",
    val screenshotUrl: String? = null
) {
    fun isStep1Valid(): Boolean = serviceName.isNotBlank() && promoCodeType != null

    fun isStep2Valid(): Boolean =
        when (promoCodeType) {
            PromoCodeType.PERCENTAGE -> promoCode.isNotBlank() &&
                discountPercentage.isNotBlank() &&
                minimumOrderAmount.isNotBlank()
            PromoCodeType.FIXED_AMOUNT -> promoCode.isNotBlank() &&
                discountAmount.isNotBlank() &&
                minimumOrderAmount.isNotBlank()
            null -> false
        }

    fun isStep3Valid(): Boolean = endDate != null && endDate.isAfter(startDate)

    fun isStep4Valid(): Boolean = title.isNotBlank()

    fun canProceedFromStep(step: SubmissionWizardStep): Boolean =
        when (step) {
            SubmissionWizardStep.SERVICE_AND_TYPE -> isStep1Valid()
            SubmissionWizardStep.TYPE_DETAILS -> isStep2Valid()
            SubmissionWizardStep.DATE_SETTINGS -> isStep3Valid()
            SubmissionWizardStep.OPTIONAL_DETAILS -> isStep4Valid()
        }
}

sealed interface SubmissionWizardUiState {
    data object Loading : SubmissionWizardUiState

    data class Success(
        val currentStep: SubmissionWizardStep,
        val wizardData: SubmissionWizardData,
        val isSubmitting: Boolean = false,
        val validationErrors: Map<String, String> = emptyMap(),
        // Service search state
        val availableServices: List<Service> = emptyList(),
        val popularServices: List<Service> = emptyList(),
        val serviceSearchResults: List<Service> = emptyList(),
        val isSearchingServices: Boolean = false,
        val serviceSearchQuery: String = ""
    ) : SubmissionWizardUiState {
        val canGoNext: Boolean get() = wizardData.canProceedFromStep(currentStep) && !isSubmitting
        val canGoPrevious: Boolean get() = !currentStep.isFirst && !isSubmitting
        val canSubmit: Boolean get() = currentStep.isLast && wizardData.isStep4Valid() && !isSubmitting
    }

    data class Error(
        val errorType: ErrorType,
        val isRetryable: Boolean,
        val shouldShowSnackbar: Boolean = true,
        val errorCode: String? = null
    ) : SubmissionWizardUiState
}
