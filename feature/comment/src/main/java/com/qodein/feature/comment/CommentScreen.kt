package com.qodein.feature.comment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeEmptyState
import com.qodein.core.designsystem.component.QodeGradient
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.CommentCard
import com.qodein.core.ui.component.CommentInput
import com.qodein.core.ui.component.QodeActionErrorCard
import com.qodein.core.ui.error.toLocalizedMessage
import com.qodein.shared.common.result.ErrorAction
import com.qodein.shared.model.CommentParentType

/**
 * Modern comment screen with MVI architecture and design system compliance
 */
@Composable
fun CommentScreen(
    parentId: String,
    parentType: String,
    postTitle: String? = null,
    postContent: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

    // Track screen view
    TrackScreenViewEvent(screenName = "comment_screen")

    // Initialize screen
    LaunchedEffect(parentId, parentType, postTitle, postContent) {
        viewModel.handleAction(
            CommentAction.Initialize(
                parentId = parentId,
                parentType = parentType,
                postTitle = postTitle,
                postContent = postContent,
            ),
        )
    }

    // Handle events
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is CommentEvent.NavigateBack -> onNavigateBack()
                is CommentEvent.NavigateToProfile -> onNavigateToProfile(event.username)
                is CommentEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is CommentEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is CommentEvent.CommentSubmitted -> {
                    // Success handled by ShowSuccess event
                }
                is CommentEvent.ScrollToBottom -> {
                    val currentState = uiState
                    if (currentState is CommentUiState.Content && currentState.comments.isNotEmpty()) {
                        lazyListState.animateScrollToItem(0) // Scroll to top (newest comment)
                    }
                }
            }
        }
    }

    // Auto-scroll detection for load more
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.lastOrNull()
                lastVisibleItem?.index != null && lastVisibleItem.index >= layoutInfo.totalItemsCount - 3
            }
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.handleAction(CommentAction.LoadMoreComments)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        QodeGradient()

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Main content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when (val currentState = uiState) {
                    is CommentUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Loading comments...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    is CommentUiState.Content -> {
                        if (currentState.isEmpty) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = SpacingTokens.md),
                                contentAlignment = Alignment.Center,
                            ) {
                                QodeEmptyState(
                                    icon = Icons.Default.ChatBubbleOutline,
                                    title = "No comments yet",
                                    description = "Be the first to share your thoughts about this ${
                                        if (currentState.parentType == CommentParentType.POST) "post" else "promo code"
                                    }!",
                                )
                            }
                        } else {
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = SpacingTokens.md,
                                    end = SpacingTokens.md,
                                    top = SpacingTokens.sm,
                                    bottom = SpacingTokens.xxl,
                                ),
                                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                            ) {
                                // Show the post being commented on if it's a post
                                if (currentState.parentType == CommentParentType.POST && currentState.post != null) {
                                    item("post_content") {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(SpacingTokens.md),
                                        ) {
                                            // Simple post content display
                                            // Show title only if it exists
                                            currentState.post.title?.let { title ->
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(bottom = SpacingTokens.sm),
                                                )
                                            }

                                            Text(
                                                text = currentState.post.content,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = SpacingTokens.md),
                                            )

                                            // Divider between post and comments
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                            )
                                        }
                                    }
                                }

                                items(
                                    items = currentState.comments,
                                    key = { it.id.value },
                                ) { comment ->
                                    CommentCard(
                                        comment = comment,
                                        onLikeClick = { commentId ->
                                            if (comment.isUpvotedByCurrentUser) {
                                                viewModel.handleAction(CommentAction.CommentUnliked(commentId))
                                            } else {
                                                viewModel.handleAction(CommentAction.CommentLiked(commentId))
                                            }
                                        },
                                        onDislikeClick = { commentId ->
                                            if (comment.isDownvotedByCurrentUser) {
                                                viewModel.handleAction(CommentAction.CommentUndisliked(commentId))
                                            } else {
                                                viewModel.handleAction(CommentAction.CommentDisliked(commentId))
                                            }
                                        },
                                        onUserClick = { username ->
                                            viewModel.handleAction(CommentAction.UserClicked(username))
                                        },
                                    )
                                }

                                // Loading more indicator
                                if (currentState.isLoadingMore) {
                                    item("loading_more") {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = SpacingTokens.md),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is CommentUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = SpacingTokens.md),
                            contentAlignment = Alignment.Center,
                        ) {
                            QodeActionErrorCard(
                                message = currentState.errorType.toLocalizedMessage(),
                                errorAction = if (currentState.isRetryable) ErrorAction.RETRY else ErrorAction.DISMISS_ONLY,
                                onActionClicked = { viewModel.handleAction(CommentAction.RetryClicked) },
                                onDismiss = { viewModel.handleAction(CommentAction.ErrorDismissed) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            // Comment input at bottom
            val currentBottomState = uiState
            if (currentBottomState is CommentUiState.Content) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(SpacingTokens.md),
                ) {
                    CommentInput(
                        text = currentBottomState.commentText,
                        onTextChange = { viewModel.handleAction(CommentAction.CommentTextChanged(it)) },
                        onSubmit = { viewModel.handleAction(CommentAction.SubmitComment) },
                        onClear = { viewModel.handleAction(CommentAction.ClearCommentText) },
                        isSubmitting = currentBottomState.isSubmittingComment,
                        canSubmit = currentBottomState.canSubmit,
                        placeholder = "Share your thoughts...",
                    )
                }
            }
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
private fun CommentScreenPreview() {
    QodeTheme {
        // Mock preview - simplified for preview purposes
        CommentScreen(
            parentId = "post_123",
            parentType = "post",
            onNavigateBack = {},
            onNavigateToProfile = {},
        )
    }
}
