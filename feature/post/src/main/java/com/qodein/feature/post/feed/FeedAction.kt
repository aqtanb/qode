package com.qodein.feature.post.feed

import com.qodein.shared.model.PostId

sealed interface FeedAction {
    data object LoadPosts : FeedAction
    data class PostClicked(val postId: PostId) : FeedAction
    data object ProfileClicked : FeedAction
    data object SettingsClicked : FeedAction
    data object RetryClicked : FeedAction
}

sealed interface FeedEvent {
    data object NavigateToProfile : FeedEvent
    data object NavigateToAuth : FeedEvent
    data object NavigateToSettings : FeedEvent
    data class NavigateToPost(val postId: PostId) : FeedEvent
}
