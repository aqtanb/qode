package com.qodein.feature.profile

import com.qodein.shared.model.PostId
import com.qodein.shared.model.PromocodeId

sealed interface ProfileAction {
    data object RetryClicked : ProfileAction
    data object BlockedClicked : ProfileAction
    data object SignOutClicked : ProfileAction

    data class TabSelected(val tab: ProfileTab) : ProfileAction

    data object LoadMorePromocodes : ProfileAction
    data object LoadMorePosts : ProfileAction

    data class PromocodeClicked(val promocodeId: PromocodeId) : ProfileAction
    data class PostClicked(val postId: PostId) : ProfileAction

    data object RetryPromocodesClicked : ProfileAction
    data object RetryPostsClicked : ProfileAction
}
