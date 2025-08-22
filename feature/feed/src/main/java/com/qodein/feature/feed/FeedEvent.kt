package com.qodein.feature.feed

import com.qodein.shared.model.PostId

/**
 * One-time events for the search screen following MVI pattern
 */
sealed class FeedEvent {
    data class NavigateToPost(val postId: PostId) : FeedEvent()
    data class NavigateToComments(val postId: PostId) : FeedEvent()
    data class NavigateToProfile(val username: String) : FeedEvent()
    data class ShowShareDialog(val postId: PostId) : FeedEvent()
    data class ShowError(val message: String) : FeedEvent()
    data class ShowSuccess(val message: String) : FeedEvent()
}
