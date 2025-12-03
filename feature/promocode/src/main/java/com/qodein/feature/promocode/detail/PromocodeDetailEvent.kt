package com.qodein.feature.promocode.detail

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Promocode

sealed class PromocodeDetailEvent {

    // Navigation Events
    data object NavigateBack : PromocodeDetailEvent()

    // System Events
    data class SharePromocode(val promoCode: Promocode) : PromocodeDetailEvent()
    data class CopyCodeToClipboard(val code: String) : PromocodeDetailEvent()

    // UI Feedback Events
    data class ShowSnackbar(val message: String) : PromocodeDetailEvent()
    data class ShowError(val error: OperationError) : PromocodeDetailEvent()
    data class ShowVoteFeedback(val isUpvote: Boolean) : PromocodeDetailEvent()
}
