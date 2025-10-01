package com.qodein.feature.post

import com.qodein.shared.model.PostId
import com.qodein.shared.model.Tag

/**
 * User actions in the search screen following MVI pattern
 */
sealed class FeedAction {
    // Search actions
    data class FeedQueryChanged(val query: String) : FeedAction()
    data object FeedSubmitted : FeedAction()
    data object ClearFeed : FeedAction()

    // Tag actions
    data class TagSelected(val tag: Tag) : FeedAction()
    data class TagRemoved(val tag: Tag) : FeedAction()
    data object ClearAllTags : FeedAction()

    // Post actions
    data class PostLiked(val postId: PostId) : FeedAction()
    data class PostUnliked(val postId: PostId) : FeedAction()
    data class PostShared(val postId: PostId) : FeedAction()
    data class PostCommentClicked(val postId: PostId) : FeedAction()
    data class PostClicked(val postId: PostId) : FeedAction()
    data class UserClicked(val username: String) : FeedAction()

    // Feed actions
    data object LoadMorePosts : FeedAction()
    data object RefreshPosts : FeedAction()

    // Error handling
    data object RetryClicked : FeedAction()
    data object ErrorDismissed : FeedAction()
}
