package com.qodein.feature.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.Comment
import com.qodein.shared.model.CommentId
import com.qodein.shared.model.CommentParentType
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.Tag
import com.qodein.shared.model.UserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper
    // TODO: Inject comment repository when available
) : ViewModel() {

    private val _state = MutableStateFlow<CommentUiState>(CommentUiState.initial())
    val state: StateFlow<CommentUiState> = _state.asStateFlow()

    private val _events = Channel<CommentEvent>(Channel.BUFFERED)
    val events: Flow<CommentEvent> = _events.receiveAsFlow()

    fun handleAction(action: CommentAction) {
        when (action) {
            is CommentAction.Initialize -> initialize(action.parentId, action.parentType, action.postTitle, action.postContent)
            is CommentAction.CommentTextChanged -> updateCommentText(action.text)
            is CommentAction.SubmitComment -> submitComment()
            is CommentAction.ClearCommentText -> clearCommentText()
            is CommentAction.CommentLiked -> likeComment(action.commentId)
            is CommentAction.CommentUnliked -> unlikeComment(action.commentId)
            is CommentAction.CommentDisliked -> dislikeComment(action.commentId)
            is CommentAction.CommentUndisliked -> undislikeComment(action.commentId)
            is CommentAction.UserClicked -> openUserProfile(action.username)
            is CommentAction.BackClicked -> navigateBack()
            is CommentAction.RefreshComments -> refreshComments()
            is CommentAction.LoadMoreComments -> loadMoreComments()
            is CommentAction.RetryClicked -> retryLastAction()
            is CommentAction.ErrorDismissed -> dismissError()
        }
    }

    private fun initialize(
        parentId: String,
        parentType: String,
        postTitle: String? = null,
        postContent: String? = null
    ) {
        val commentParentType = when (parentType.lowercase()) {
            "post" -> CommentParentType.POST
            "promo_code", "promocode" -> CommentParentType.PROMO_CODE
            else -> CommentParentType.POST
        }

        _state.value = CommentUiState.Loading(
            parentId = parentId,
            parentType = commentParentType,
        )

        loadComments(parentId, commentParentType, postTitle, postContent)
    }

    private fun loadComments(
        parentId: String,
        parentType: CommentParentType,
        postTitle: String? = null,
        postContent: String? = null
    ) {
        viewModelScope.launch {
            try {
                // Track analytics
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "comments_viewed",
                        extras = listOf(
                            AnalyticsEvent.Param("parent_id", parentId),
                            AnalyticsEvent.Param("parent_type", parentType.name),
                        ),
                    ),
                )

                // TODO: Replace with actual repository call
                delay(800) // Simulate network delay
                val comments = generateMockComments(parentId, parentType)
                val post = if (parentType == CommentParentType.POST && postTitle != null && postContent != null) {
                    // Use the actual post data passed from navigation
                    Post(
                        id = PostId(parentId),
                        authorId = UserId("unknown_author"),
                        authorUsername = "Unknown Author",
                        authorAvatarUrl = null,
                        title = postTitle,
                        content = postContent,
                        imageUrls = emptyList(),
                        tags = emptyList(),
                        upvotes = 0,
                        downvotes = 0,
                        shares = 0,
                        createdAt = Clock.System.now(),
                        isUpvotedByCurrentUser = false,
                        isDownvotedByCurrentUser = false,
                    )
                } else if (parentType == CommentParentType.POST) {
                    // Fallback to mock data if post data not provided
                    generateMockPost(parentId)
                } else {
                    null
                }

                _state.value = CommentUiState.Content(
                    parentId = parentId,
                    parentType = parentType,
                    comments = comments,
                    post = post,
                    hasMoreComments = comments.size >= 10,
                    canSubmitComment = true,
                )
            } catch (e: Exception) {
                _state.value = CommentUiState.Error(
                    parentId = parentId,
                    parentType = parentType,
                    errorType = e.toErrorType(),
                    isRetryable = e.isRetryable(),
                    shouldShowSnackbar = e.shouldShowSnackbar(),
                    errorCode = e.getErrorCode(),
                )
                emitEvent(CommentEvent.ShowError("Failed to load comments"))
            }
        }
    }

    private fun updateCommentText(text: String) {
        _state.value = when (val currentState = _state.value) {
            is CommentUiState.Loading -> currentState.copy(commentText = text)
            is CommentUiState.Content -> currentState.copy(commentText = text)
            is CommentUiState.Error -> currentState.copy(commentText = text)
        }
    }

    private fun submitComment() {
        val currentState = _state.value
        if (currentState.commentText.isBlank() || currentState.isSubmittingComment) return

        viewModelScope.launch {
            // Update state to show submitting
            _state.value = when (currentState) {
                is CommentUiState.Loading -> currentState.copy(isSubmittingComment = true)
                is CommentUiState.Content -> currentState.copy(isSubmittingComment = true)
                is CommentUiState.Error -> currentState.copy(isSubmittingComment = true)
            }

            try {
                // Track analytics
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "comment_submitted",
                        extras = listOf(
                            AnalyticsEvent.Param("parent_id", currentState.parentId),
                            AnalyticsEvent.Param("parent_type", currentState.parentType.name),
                            AnalyticsEvent.Param("comment_length", currentState.commentText.length.toString()),
                        ),
                    ),
                )

                // TODO: Replace with actual repository call
                delay(1000) // Simulate network delay
                val newComment = createMockComment(
                    parentId = currentState.parentId,
                    parentType = currentState.parentType,
                    content = currentState.commentText.trim(),
                )

                // Update state with new comment
                val updatedState = when (currentState) {
                    is CommentUiState.Content -> currentState.copy(
                        comments = listOf(newComment) + currentState.comments,
                        commentText = "",
                        isSubmittingComment = false,
                    )
                    else -> CommentUiState.Content(
                        parentId = currentState.parentId,
                        parentType = currentState.parentType,
                        comments = listOf(newComment),
                        commentText = "",
                        isSubmittingComment = false,
                        canSubmitComment = true,
                    )
                }

                _state.value = updatedState
                emitEvent(CommentEvent.CommentSubmitted)
                emitEvent(CommentEvent.ScrollToBottom)
                emitEvent(CommentEvent.ShowSuccess("Comment posted successfully"))
            } catch (e: Exception) {
                _state.value = when (currentState) {
                    is CommentUiState.Loading -> currentState.copy(isSubmittingComment = false)
                    is CommentUiState.Content -> currentState.copy(isSubmittingComment = false)
                    is CommentUiState.Error -> currentState.copy(isSubmittingComment = false)
                }
                emitEvent(CommentEvent.ShowError("Failed to post comment"))
            }
        }
    }

    private fun clearCommentText() {
        _state.value = when (val currentState = _state.value) {
            is CommentUiState.Loading -> currentState.copy(commentText = "")
            is CommentUiState.Content -> currentState.copy(commentText = "")
            is CommentUiState.Error -> currentState.copy(commentText = "")
        }
    }

    private fun likeComment(commentId: CommentId) {
        viewModelScope.launch {
            try {
                // Track analytics
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "comment_liked",
                        extras = listOf(
                            AnalyticsEvent.Param("comment_id", commentId.value),
                        ),
                    ),
                )

                // TODO: Replace with actual repository call
                updateCommentInState(commentId) { comment ->
                    comment.copy(
                        upvotes = if (comment.isUpvotedByCurrentUser) comment.upvotes else comment.upvotes + 1,
                        downvotes = if (comment.isDownvotedByCurrentUser) comment.downvotes - 1 else comment.downvotes,
                        isUpvotedByCurrentUser = !comment.isUpvotedByCurrentUser,
                        isDownvotedByCurrentUser = false,
                    )
                }
            } catch (e: Exception) {
                emitEvent(CommentEvent.ShowError("Failed to like comment"))
            }
        }
    }

    private fun unlikeComment(commentId: CommentId) {
        likeComment(commentId) // Toggle behavior
    }

    private fun dislikeComment(commentId: CommentId) {
        viewModelScope.launch {
            try {
                // Track analytics
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "comment_disliked",
                        extras = listOf(
                            AnalyticsEvent.Param("comment_id", commentId.value),
                        ),
                    ),
                )

                // TODO: Replace with actual repository call
                updateCommentInState(commentId) { comment ->
                    comment.copy(
                        downvotes = if (comment.isDownvotedByCurrentUser) comment.downvotes else comment.downvotes + 1,
                        upvotes = if (comment.isUpvotedByCurrentUser) comment.upvotes - 1 else comment.upvotes,
                        isDownvotedByCurrentUser = !comment.isDownvotedByCurrentUser,
                        isUpvotedByCurrentUser = false,
                    )
                }
            } catch (e: Exception) {
                emitEvent(CommentEvent.ShowError("Failed to dislike comment"))
            }
        }
    }

    private fun undislikeComment(commentId: CommentId) {
        dislikeComment(commentId) // Toggle behavior
    }

    private fun openUserProfile(username: String) {
        viewModelScope.launch {
            // Track analytics
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "user_profile_clicked",
                    extras = listOf(
                        AnalyticsEvent.Param("username", username),
                        AnalyticsEvent.Param("source", "comments"),
                    ),
                ),
            )

            emitEvent(CommentEvent.NavigateToProfile(username))
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            emitEvent(CommentEvent.NavigateBack)
        }
    }

    private fun refreshComments() {
        val currentState = _state.value
        if (currentState is CommentUiState.Content) {
            _state.value = currentState.copy(isRefreshing = true)
            loadComments(currentState.parentId, currentState.parentType)
        }
    }

    private fun loadMoreComments() {
        val currentState = _state.value
        if (currentState !is CommentUiState.Content || currentState.isLoadingMore || !currentState.hasMoreComments) return

        _state.value = currentState.copy(isLoadingMore = true)

        viewModelScope.launch {
            try {
                // TODO: Replace with actual repository call for pagination
                delay(1000) // Simulate network delay
                val moreComments = generateMockComments(currentState.parentId, currentState.parentType, offset = currentState.comments.size)

                _state.value = currentState.copy(
                    comments = currentState.comments + moreComments,
                    isLoadingMore = false,
                    hasMoreComments = moreComments.size >= 10,
                )
            } catch (e: Exception) {
                _state.value = currentState.copy(isLoadingMore = false)
                emitEvent(CommentEvent.ShowError("Failed to load more comments"))
            }
        }
    }

    private fun retryLastAction() {
        val currentState = _state.value
        if (currentState is CommentUiState.Error) {
            loadComments(currentState.parentId, currentState.parentType)
        }
    }

    private fun dismissError() {
        // Error dismissal is handled by the UI layer through state changes
    }

    private fun updateCommentInState(
        commentId: CommentId,
        transform: (Comment) -> Comment
    ) {
        _state.value = when (val currentState = _state.value) {
            is CommentUiState.Content -> {
                val updatedComments = currentState.comments.map { comment ->
                    if (comment.id == commentId) transform(comment) else comment
                }
                currentState.copy(comments = updatedComments)
            }
            else -> currentState
        }
    }

    private suspend fun emitEvent(event: CommentEvent) {
        _events.send(event)
    }

    // Mock data generation - TODO: Remove when repository is implemented
    private fun generateMockComments(
        parentId: String,
        parentType: CommentParentType,
        offset: Int = 0
    ): List<Comment> {
        val mockUsers = listOf("deal_hunter", "bargain_finder", "promo_expert", "discount_lover", "code_master")
        val mockComments = listOf(
            "This is amazing! Thanks for sharing.",
            "Works perfectly for me, saved €15!",
            "Code expired for me unfortunately 😕",
            "Great find! Just used it successfully.",
            "Does this work in other countries too?",
            "Perfect timing, was looking for this!",
            "Used it yesterday, still working 👍",
            "Quality post, very helpful",
            "Can confirm this works in Germany",
            "Tried multiple times but didn't work",
        )

        return (0..4).map { index ->
            val actualIndex = offset + index
            Comment(
                id = CommentId("comment_$actualIndex"),
                parentId = parentId,
                parentType = parentType,
                authorId = UserId("user_${actualIndex % mockUsers.size}"),
                authorUsername = mockUsers[actualIndex % mockUsers.size],
                authorAvatarUrl = "https://picsum.photos/seed/comment$actualIndex/150/150",
                content = mockComments[actualIndex % mockComments.size],
                upvotes = (1..25).random(),
                downvotes = (0..5).random(),
                createdAt = Clock.System.now().minus(Duration.parse("${(1..48).random()}h")),
                isUpvotedByCurrentUser = (0..10).random() < 2,
                isDownvotedByCurrentUser = (0..20).random() < 1,
            )
        }
    }

    // Mock post generation - TODO: Remove when repository is implemented
    private fun generateMockPost(postId: String): Post =
        Post(
            id = PostId(postId),
            authorId = UserId("user_${(1..100).random()}"),
            authorUsername = listOf("alex", "sam", "jordan", "casey", "riley", "morgan").random(),
            authorAvatarUrl = "https://picsum.photos/seed/user${(1..100).random()}/150/150",
            title = "Sample Post Title",
            content = "This is a sample post content that is being commented on. " +
                "It demonstrates how the post appears above the comments in the comment screen.",
            imageUrls = if ((1..10).random() <= 3) listOf("https://picsum.photos/seed/post$postId/600/400") else emptyList(),
            tags = listOf(
                Tag.create("discussion"),
                Tag.create("sample"),
                Tag.create("ui"),
            ).shuffled().take((1..3).random()),
            upvotes = (5..100).random(),
            downvotes = (0..10).random(),
            shares = (0..20).random(),
            createdAt = Clock.System.now().minus(Duration.parse("${(1..72).random()}h")),
            isUpvotedByCurrentUser = (0..10).random() < 2,
            isDownvotedByCurrentUser = (0..20).random() < 1,
        )

    private fun createMockComment(
        parentId: String,
        parentType: CommentParentType,
        content: String
    ): Comment =
        Comment(
            id = CommentId("new_comment_${System.currentTimeMillis()}"),
            parentId = parentId,
            parentType = parentType,
            authorId = UserId("current_user"),
            authorUsername = "current_user",
            authorAvatarUrl = "https://picsum.photos/seed/currentuser/150/150",
            content = content,
            upvotes = 0,
            downvotes = 0,
            createdAt = Clock.System.now(),
            isUpvotedByCurrentUser = false,
            isDownvotedByCurrentUser = false,
        )
}

// Extension functions for error handling
private fun Throwable.toErrorType(): ErrorType = ErrorType.NETWORK_GENERAL
private fun Throwable.isRetryable(): Boolean = true
private fun Throwable.shouldShowSnackbar(): Boolean = true
private fun Throwable.getErrorCode(): String? = null
