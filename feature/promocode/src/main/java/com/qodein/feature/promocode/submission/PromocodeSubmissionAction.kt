package com.qodein.feature.promocode.submission

import android.content.Context
import com.qodein.shared.model.User
import java.time.LocalDate

sealed interface PromocodeSubmissionAction {
    // Progressive step navigation
    data object NextProgressiveStep : PromocodeSubmissionAction
    data object PreviousProgressiveStep : PromocodeSubmissionAction
    data class NavigateToStep(val step: PromocodeSubmissionStep) : PromocodeSubmissionAction

    // Service selection UI actions
    data object ShowServiceSelector : PromocodeSubmissionAction
    data object HideServiceSelector : PromocodeSubmissionAction
    data object ToggleManualEntry : PromocodeSubmissionAction

    // Step 1: Core Details actions
    data class UpdateServiceName(val serviceName: String) : PromocodeSubmissionAction // For manual entry
    data class UpdatePromoCodeType(val type: PromoCodeType) : PromocodeSubmissionAction
    data class UpdatePromoCode(val promoCode: String) : PromocodeSubmissionAction
    data class UpdateDiscountPercentage(val percentage: String) : PromocodeSubmissionAction
    data class UpdateDiscountAmount(val amount: String) : PromocodeSubmissionAction
    data class UpdateMinimumOrderAmount(val amount: String) : PromocodeSubmissionAction
    data class UpdateFirstUserOnly(val isFirstUserOnly: Boolean) : PromocodeSubmissionAction
    data class UpdateOneTimeUseOnly(val isOneTimeUseOnly: Boolean) : PromocodeSubmissionAction
    data class UpdateDescription(val description: String) : PromocodeSubmissionAction

    // Step 2: Date Settings actions
    data class UpdateStartDate(val date: LocalDate) : PromocodeSubmissionAction
    data class UpdateEndDate(val date: LocalDate) : PromocodeSubmissionAction

    data class SubmitPromoCodeWithUser(val user: User) : PromocodeSubmissionAction
    data object SubmitPromoCode : PromocodeSubmissionAction

    // Authentication actions
    data class SignInWithGoogle(val context: Context) : PromocodeSubmissionAction
    data object DismissAuthSheet : PromocodeSubmissionAction

    // Error handling
    data object RetryClicked : PromocodeSubmissionAction
    data object ClearValidationErrors : PromocodeSubmissionAction
}
