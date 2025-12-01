package com.qodein.feature.promocode.submission

import android.content.Context
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep
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

    data class UpdateServiceName(val serviceName: String) : PromocodeSubmissionAction
    data class UpdatePromocodeType(val type: PromocodeType) : PromocodeSubmissionAction
    data class UpdatePromocode(val promocode: String) : PromocodeSubmissionAction
    data class UpdateDiscountPercentage(val percentage: String) : PromocodeSubmissionAction
    data class UpdateDiscountAmount(val amount: String) : PromocodeSubmissionAction
    data class UpdateMinimumOrderAmount(val amount: String) : PromocodeSubmissionAction
    data class UpdateFirstUserOnly(val isFirstUserOnly: Boolean) : PromocodeSubmissionAction
    data class UpdateOneTimeUseOnly(val isOneTimeUseOnly: Boolean) : PromocodeSubmissionAction
    data class UpdateDescription(val description: String) : PromocodeSubmissionAction
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
