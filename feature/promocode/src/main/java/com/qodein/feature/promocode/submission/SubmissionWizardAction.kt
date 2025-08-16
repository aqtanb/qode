package com.qodein.feature.promocode.submission

import java.time.LocalDate

sealed interface SubmissionWizardAction {
    // Navigation actions
    data object GoToNextStep : SubmissionWizardAction
    data object GoToPreviousStep : SubmissionWizardAction
    data class GoToStep(val step: SubmissionWizardStep) : SubmissionWizardAction

    // Step 1: Service & Type actions
    data class UpdateServiceName(val serviceName: String) : SubmissionWizardAction
    data class UpdatePromoCodeType(val type: PromoCodeType) : SubmissionWizardAction
    data class SearchServices(val query: String) : SubmissionWizardAction

    // Step 2: Type Details actions
    data class UpdatePromoCode(val promoCode: String) : SubmissionWizardAction
    data class UpdateDiscountPercentage(val percentage: String) : SubmissionWizardAction
    data class UpdateDiscountAmount(val amount: String) : SubmissionWizardAction
    data class UpdateMinimumOrderAmount(val amount: String) : SubmissionWizardAction
    data class UpdateFirstUserOnly(val isFirstUserOnly: Boolean) : SubmissionWizardAction

    // Step 3: Date Settings actions
    data class UpdateStartDate(val date: LocalDate) : SubmissionWizardAction
    data class UpdateEndDate(val date: LocalDate) : SubmissionWizardAction

    // Step 4: Optional Details actions
    data class UpdateTitle(val title: String) : SubmissionWizardAction
    data class UpdateDescription(val description: String) : SubmissionWizardAction
    data class UpdateScreenshotUrl(val url: String?) : SubmissionWizardAction

    // Form submission
    data object SubmitPromoCode : SubmissionWizardAction

    // Error handling
    data object RetryClicked : SubmissionWizardAction
    data object ClearValidationErrors : SubmissionWizardAction
}
