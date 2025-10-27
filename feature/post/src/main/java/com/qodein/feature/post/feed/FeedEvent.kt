package com.qodein.feature.post.feed

import com.qodein.shared.model.PostId

/**
 * One-time events for the search screen following MVI pattern
 */
sealed class FeedEvent {
    data object NavigateToProfile : FeedEvent()
    data object NavigateToSettings : FeedEvent()
    data class NavigateToPost(val postId: PostId) : FeedEvent()
}
