package com.qodein.feature.promocode.submission

import com.qodein.shared.model.Service
import java.time.LocalDate

sealed interface SubmissionWizardAction {
    // Progressive step navigation
    data object NextProgressiveStep : SubmissionWizardAction
    data object PreviousProgressiveStep : SubmissionWizardAction

    // Service selection UI actions
    data object ShowServiceSelector : SubmissionWizardAction
    data object HideServiceSelector : SubmissionWizardAction
    data object ToggleManualEntry : SubmissionWizardAction

    // Step 1: Core Details actions
    data class SelectService(val service: Service) : SubmissionWizardAction
    data class UpdateServiceName(val serviceName: String) : SubmissionWizardAction // For manual entry
    data class UpdatePromoCodeType(val type: PromoCodeType) : SubmissionWizardAction
    data class SearchServices(val query: String) : SubmissionWizardAction
    data class UpdatePromoCode(val promoCode: String) : SubmissionWizardAction
    data class UpdateDiscountPercentage(val percentage: String) : SubmissionWizardAction
    data class UpdateDiscountAmount(val amount: String) : SubmissionWizardAction
    data class UpdateMinimumOrderAmount(val amount: String) : SubmissionWizardAction
    data class UpdateFirstUserOnly(val isFirstUserOnly: Boolean) : SubmissionWizardAction
    data class UpdateDescription(val description: String) : SubmissionWizardAction

    // Step 2: Date Settings actions
    data class UpdateStartDate(val date: LocalDate) : SubmissionWizardAction
    data class UpdateEndDate(val date: LocalDate) : SubmissionWizardAction

    // Form submission
    data object SubmitPromoCode : SubmissionWizardAction

    // Error handling
    data object RetryClicked : SubmissionWizardAction
    data object ClearValidationErrors : SubmissionWizardAction
}
