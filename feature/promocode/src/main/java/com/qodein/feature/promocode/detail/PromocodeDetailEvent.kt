package com.qodein.feature.promocode.detail

import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId

sealed class PromocodeDetailEvent {

    // Navigation Events
    data object NavigateBack : PromocodeDetailEvent()
    data class NavigateToComments(val promoCodeId: PromoCodeId) : PromocodeDetailEvent()
    data class NavigateToService(val serviceName: String) : PromocodeDetailEvent()

    // System Events
    data class SharePromocode(val promoCode: PromoCode) : PromocodeDetailEvent()
    data class CopyCodeToClipboard(val code: String) : PromocodeDetailEvent()

    // UI Feedback Events
    data class ShowSnackbar(val message: String) : PromocodeDetailEvent()
    data class ShowVoteFeedback(val isUpvote: Boolean) : PromocodeDetailEvent()

    // Follow Events (TODO implementations as requested)
    data class ShowFollowServiceTodo(val serviceName: String) : PromocodeDetailEvent()
    data class ShowFollowCategoryTodo(val categoryName: String) : PromocodeDetailEvent()
}
