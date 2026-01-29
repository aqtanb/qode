package com.qodein.feature.promocode.submission

import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import com.qodein.shared.model.Service
import java.time.LocalDate

enum class PromocodeType {
    PERCENTAGE,
    FIXED_AMOUNT,
    FREE_ITEM
}

data class SubmissionWizardData(
    val selectedService: Service? = null,
    val serviceName: String = "",
    val serviceUrl: String = "",
    val isManualServiceEntry: Boolean = false,
    val promocode: String = "",
    val promocodeType: PromocodeType? = null,
    val discountPercentage: String = "",
    val discountAmount: String = "",
    val freeItemDescription: String = "",
    val minimumOrderAmount: String = "",
    val description: String = "",
    val imageUris: List<String> = emptyList(),
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = LocalDate.now().plusDays(30)
) {
    val effectiveServiceName: String get() = selectedService?.name ?: serviceName
    val effectiveServiceUrl: String get() = selectedService?.siteUrl ?: serviceUrl
}

data class PromocodeSubmissionUiState(
    val currentStep: PromocodeWizardStep = PromocodeWizardStep.SERVICE,
    val wizardData: SubmissionWizardData = SubmissionWizardData(),
    val isCompressing: Boolean = false,
    val serviceConfirmationDialog: ServiceConfirmationDialogState? = null
) {
    val canGoNext: Boolean get() = !isCompressing && !currentStep.isLast &&
        (currentStep.canProceed(wizardData) || !currentStep.isRequired)

    val canGoPrevious: Boolean get() = !isCompressing && !currentStep.isFirst

    val canSubmit: Boolean get() = !isCompressing &&
        allRequiredStepsComplete() &&
        currentStep.canProceed(wizardData)

    private fun allRequiredStepsComplete(): Boolean =
        PromocodeWizardStep.entries
            .filter { it.isRequired }
            .all { step ->
                step.stepNumber <= currentStep.stepNumber && step.canProceed(wizardData)
            }
}

data class ServiceConfirmationDialogState(val serviceName: String, val serviceUrl: String, val logoUrl: String)
