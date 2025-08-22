package com.qodein.feature.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.QodeActionErrorCard
import com.qodein.core.ui.error.toLocalizedMessage
import com.qodein.feature.feed.component.PostCard
import com.qodein.feature.feed.component.SearchBar
import com.qodein.shared.common.result.ErrorAction

/**
 * Modern feed screen with post feed and tag-based filtering
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "Feed")

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()

    // Handle events
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is FeedEvent.NavigateToPost -> {
                    // TODO: Handle navigation to post details
                }
                is FeedEvent.NavigateToComments -> {
                    // TODO: Handle navigation to comments
                }
                is FeedEvent.NavigateToProfile -> {
                    // TODO: Handle navigation to user profile
                }
                is FeedEvent.ShowShareDialog -> {
                    // TODO: Handle share dialog
                }
                is FeedEvent.ShowError -> {
                    // TODO: Show error snackbar/toast
                }
                is FeedEvent.ShowSuccess -> {
                    // TODO: Show success snackbar/toast
                }
            }
        }
    }

    // Detect when user scrolls near bottom for pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            val currentState = uiState
            lastVisibleItemIndex > (totalItemsNumber - 3) &&
                currentState is FeedUiState.Content &&
                currentState.hasMorePosts
        }
    }

    LaunchedEffect(shouldLoadMore) {
        val currentState = uiState
        if (shouldLoadMore && currentState is FeedUiState.Content && !currentState.isLoadingMore) {
            viewModel.handleAction(FeedAction.LoadMorePosts)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.md),
    ) {
        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        // Search bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = { viewModel.handleAction(FeedAction.FeedQueryChanged(it)) },
            onSearchSubmit = { viewModel.handleAction(FeedAction.FeedSubmitted) },
            onClearSearch = { viewModel.handleAction(FeedAction.ClearFeed) },
        )

        Spacer(modifier = Modifier.height(SpacingTokens.md))

        // Enhanced active filters section
        AnimatedVisibility(
            visible = uiState.selectedTags.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SpacingTokens.xs),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shadowElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.padding(SpacingTokens.md),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                    ),
                            )
                            Text(
                                text = "Active filters (${uiState.selectedTags.size})",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Surface(
                            onClick = { viewModel.handleAction(FeedAction.ClearAllTags) },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        ) {
                            Text(
                                text = "Clear all",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp,
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(SpacingTokens.sm))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        uiState.selectedTags.forEach { tag ->
                            val tagColor = tag.color?.let { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it)) }
                                ?: androidx.compose.ui.graphics.Color(0xFF6C63FF)

                            Surface(
                                onClick = {},
                                shape = RoundedCornerShape(20.dp),
                                color = tagColor.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    tagColor.copy(alpha = 0.4f),
                                ),
                                shadowElevation = 1.dp,
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        start = 14.dp,
                                        end = 8.dp,
                                        top = 8.dp,
                                        bottom = 8.dp,
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = "#${tag.name}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                        ),
                                        color = tagColor,
                                    )

                                    Surface(
                                        onClick = { viewModel.handleAction(FeedAction.TagRemoved(tag)) },
                                        shape = CircleShape,
                                        color = tagColor.copy(alpha = 0.2f),
                                        modifier = Modifier.size(20.dp),
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize(),
                                        ) {
                                            Text(
                                                text = "×",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tagColor,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Enhanced suggested tags section
        AnimatedVisibility(
            visible = uiState.searchQuery.isBlank() && uiState.suggestedTags.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = androidx.compose.ui.graphics.Color(0xFF4ECDC4),
                                shape = CircleShape,
                            ),
                    )
                    Text(
                        text = "Trending tags",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "🔥",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.suggestedTags.take(8).forEach { tag ->
                        val tagColor = tag.color?.let { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it)) }
                            ?: androidx.compose.ui.graphics.Color(0xFF6C63FF)

                        Surface(
                            onClick = { viewModel.handleAction(FeedAction.TagSelected(tag)) },
                            shape = RoundedCornerShape(24.dp),
                            color = tagColor.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                tagColor.copy(alpha = 0.2f),
                            ),
                            shadowElevation = 1.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp,
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = tagColor,
                                            shape = CircleShape,
                                        ),
                                )

                                Text(
                                    text = "#${tag.name}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                    ),
                                    color = tagColor,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.lg))
            }
        }

        // Posts feed
        when (val currentState = uiState) {
            is FeedUiState.Loading -> {
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
                            text = "Loading posts...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            is FeedUiState.Content -> {
                if (currentState.isEmpty) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        QodeEmptyState(
                            icon = Icons.Default.Search,
                            title = if (currentState.hasFilters) "No posts found" else "Start exploring",
                            description = if (currentState.hasFilters) {
                                "Try adjusting your search or tags to find more posts."
                            } else {
                                "Search for posts by tags or content to discover what the community is talking about!"
                            },
                        )
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                        contentPadding = PaddingValues(bottom = SpacingTokens.xl),
                    ) {
                        items(
                            items = currentState.posts,
                            key = { it.id.value },
                        ) { post ->
                            PostCard(
                                post = post,
                                onLikeClick = { postId ->
                                    if (post.isLikedByCurrentUser) {
                                        viewModel.handleAction(FeedAction.PostUnliked(postId))
                                    } else {
                                        viewModel.handleAction(FeedAction.PostLiked(postId))
                                    }
                                },
                                onCommentClick = { postId ->
                                    viewModel.handleAction(FeedAction.PostCommentClicked(postId))
                                },
                                onShareClick = { postId ->
                                    viewModel.handleAction(FeedAction.PostShared(postId))
                                },
                                onUserClick = { username ->
                                    // TODO: Navigate to user profile
                                },
                                onTagClick = { tag ->
                                    viewModel.handleAction(FeedAction.TagSelected(tag))
                                },
                            )
                        }

                        // Loading more indicator
                        if (currentState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(SpacingTokens.md),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }

            is FeedUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    QodeActionErrorCard(
                        message = currentState.errorType.toLocalizedMessage(),
                        errorAction = if (currentState.isRetryable) ErrorAction.RETRY else ErrorAction.DISMISS_ONLY,
                        onActionClicked = { viewModel.handleAction(FeedAction.RetryClicked) },
                        onDismiss = { viewModel.handleAction(FeedAction.ErrorDismissed) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.md),
                    )
                }
            }
        }
    }
}

// Preview with mock data
@Preview(name = "Feed Screen", showBackground = true)
@Composable
private fun FeedScreenPreview() {
    QodeTheme {
        // Note: This preview won't show posts since we don't have a mock ViewModel
        // In a real implementation, you'd create a preview-specific version
        Column {
            SearchBar(
                query = "",
                onQueryChange = {},
                onSearchSubmit = {},
                onClearSearch = {},
            )
        }
    }
}
