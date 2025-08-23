package com.qodein.feature.comment

import com.qodein.shared.model.CommentId

/**
 * User actions in the comment screen following MVI pattern
 */
sealed class CommentAction {
    // Screen initialization
    data class Initialize(val parentId: String, val parentType: String, val postTitle: String? = null, val postContent: String? = null) :
        CommentAction()

    // Comment creation
    data class CommentTextChanged(val text: String) : CommentAction()
    data object SubmitComment : CommentAction()
    data object ClearCommentText : CommentAction()

    // Comment interactions
    data class CommentLiked(val commentId: CommentId) : CommentAction()
    data class CommentUnliked(val commentId: CommentId) : CommentAction()
    data class CommentDisliked(val commentId: CommentId) : CommentAction()
    data class CommentUndisliked(val commentId: CommentId) : CommentAction()

    // Navigation
    data class UserClicked(val username: String) : CommentAction()
    data object BackClicked : CommentAction()

    // Loading and refreshing
    data object RefreshComments : CommentAction()
    data object LoadMoreComments : CommentAction()

    // Error handling
    data object RetryClicked : CommentAction()
    data object ErrorDismissed : CommentAction()
}
