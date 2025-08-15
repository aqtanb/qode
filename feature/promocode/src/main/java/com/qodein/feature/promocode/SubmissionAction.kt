package com.qodein.feature.promocode

sealed interface SubmissionAction {

    data class UpdateServiceName(val serviceName: String) : SubmissionAction

    data class UpdatePromoCode(val promoCode: String) : SubmissionAction

    data class UpdateDescription(val description: String) : SubmissionAction

    data object SubmitPromoCode : SubmissionAction

    data object RetryClicked : SubmissionAction

    data object ErrorDismissed : SubmissionAction
}
