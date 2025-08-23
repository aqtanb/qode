package com.qodein.feature.comment

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.Comment
import com.qodein.shared.model.CommentParentType
import com.qodein.shared.model.Post

/**
 * UI state for the comment screen following MVI pattern
 */
sealed interface CommentUiState {
    val parentId: String
    val parentType: CommentParentType
    val commentText: String
    val isSubmittingComment: Boolean

    data class Loading(
        override val parentId: String = "",
        override val parentType: CommentParentType = CommentParentType.POST,
        override val commentText: String = "",
        override val isSubmittingComment: Boolean = false
    ) : CommentUiState

    data class Content(
        override val parentId: String = "",
        override val parentType: CommentParentType = CommentParentType.POST,
        override val commentText: String = "",
        override val isSubmittingComment: Boolean = false,
        val comments: List<Comment> = emptyList(),
        val post: Post? = null, // The post being commented on (if parentType is POST)
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMoreComments: Boolean = true,
        val canSubmitComment: Boolean = false
    ) : CommentUiState {
        val isEmpty: Boolean get() = comments.isEmpty()
        val hasContent: Boolean get() = comments.isNotEmpty()
        val canSubmit: Boolean get() = commentText.isNotBlank() && commentText.trim().length >= 3 && !isSubmittingComment
    }

    data class Error(
        override val parentId: String = "",
        override val parentType: CommentParentType = CommentParentType.POST,
        override val commentText: String = "",
        override val isSubmittingComment: Boolean = false,
        val errorType: ErrorType,
        val isRetryable: Boolean = false,
        val shouldShowSnackbar: Boolean = false,
        val errorCode: String? = null
    ) : CommentUiState

    companion object {
        fun initial(): CommentUiState = Loading()
    }
}
