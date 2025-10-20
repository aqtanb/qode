package com.qodein.feature.post.feed

import com.qodein.shared.model.PostId

/**
 * User actions in the search screen following MVI pattern
 */
sealed class FeedAction {
    data object LoadPosts : FeedAction()
    data class PostClicked(val postId: PostId) : FeedAction()
}
