package com.qodein.feature.promocode.detail

import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.UserId

sealed class PromocodeDetailAction {

    // Data Loading Actions
    data class LoadPromocode(val promoCodeId: PromoCodeId) : PromocodeDetailAction()
    data class LoadPromocodeWithUser(val promoCodeId: PromoCodeId, val userId: UserId) : PromocodeDetailAction()
    data object RefreshData : PromocodeDetailAction()

    // Voting Actions
    data object UpvoteClicked : PromocodeDetailAction()
    data object DownvoteClicked : PromocodeDetailAction()

    // Interaction Actions
    data object CopyCodeClicked : PromocodeDetailAction()
    data object ShareClicked : PromocodeDetailAction()
    data object BookmarkToggleClicked : PromocodeDetailAction()
    data object CommentsClicked : PromocodeDetailAction()

    // Follow Actions (TODO implementations as requested)
    data object FollowServiceClicked : PromocodeDetailAction()
    data object FollowCategoryClicked : PromocodeDetailAction()

    // Navigation Actions
    data object BackClicked : PromocodeDetailAction()
    data object ServiceClicked : PromocodeDetailAction()

    // Authentication Actions
    data object SignInWithGoogleClicked : PromocodeDetailAction()
    data object DismissAuthSheet : PromocodeDetailAction()

    // Error Handling Actions
    data object RetryClicked : PromocodeDetailAction()
    data object ErrorDismissed : PromocodeDetailAction()
}
