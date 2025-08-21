package com.qodein.feature.search

import com.qodein.shared.model.PostId
import com.qodein.shared.model.Tag

/**
 * User actions in the search screen following MVI pattern
 */
sealed class SearchAction {
    // Search actions
    data class SearchQueryChanged(val query: String) : SearchAction()
    data object SearchSubmitted : SearchAction()
    data object ClearSearch : SearchAction()

    // Tag actions
    data class TagSelected(val tag: Tag) : SearchAction()
    data class TagRemoved(val tag: Tag) : SearchAction()
    data object ClearAllTags : SearchAction()

    // Post actions
    data class PostLiked(val postId: PostId) : SearchAction()
    data class PostUnliked(val postId: PostId) : SearchAction()
    data class PostShared(val postId: PostId) : SearchAction()
    data class PostCommentClicked(val postId: PostId) : SearchAction()

    // Feed actions
    data object LoadMorePosts : SearchAction()
    data object RefreshPosts : SearchAction()

    // Error handling
    data object RetryClicked : SearchAction()
    data object ErrorDismissed : SearchAction()
}
