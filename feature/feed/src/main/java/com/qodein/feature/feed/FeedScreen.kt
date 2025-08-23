package com.qodein.feature.feed

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeEmptyState
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
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
        modifier = modifier.fillMaxSize(),
    ) {
        // Fixed search bar at top
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
        }

        // Scrollable posts section
        when (val currentState = uiState) {
            is FeedUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = SpacingTokens.md),
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = SpacingTokens.md),
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
                        contentPadding = PaddingValues(
                            start = SpacingTokens.md,
                            end = SpacingTokens.md,
                            bottom = SpacingTokens.xxl,
                        ),
                    ) {
                        // Active filters section
                        if (currentState.selectedTags.isNotEmpty()) {
                            item("active_filters") {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = SpacingTokens.sm),
                                    shape = RoundedCornerShape(ShapeTokens.Corner.large),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
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
                                                        .size(SpacingTokens.sm)
                                                        .background(
                                                            color = MaterialTheme.colorScheme.primary,
                                                            shape = CircleShape,
                                                        ),
                                                )
                                                Text(
                                                    text = "Active filters (${currentState.selectedTags.size})",
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                            }

                                            Surface(
                                                onClick = { viewModel.handleAction(FeedAction.ClearAllTags) },
                                                shape = RoundedCornerShape(ShapeTokens.Corner.large),
                                                color = MaterialTheme.colorScheme.errorContainer,
                                            ) {
                                                Text(
                                                    text = "Clear all",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.padding(
                                                        horizontal = SpacingTokens.Chip.horizontalPadding,
                                                        vertical = SpacingTokens.xs,
                                                    ),
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(SpacingTokens.sm))

                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Chip.spacing),
                                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                                        ) {
                                            currentState.selectedTags.forEach { tag ->
                                                Surface(
                                                    onClick = {},
                                                    shape = RoundedCornerShape(ShapeTokens.Corner.large),
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                                    ),
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(
                                                            start = SpacingTokens.Chip.horizontalPadding,
                                                            end = SpacingTokens.sm,
                                                            top = SpacingTokens.sm,
                                                            bottom = SpacingTokens.sm,
                                                        ),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                                                    ) {
                                                        Text(
                                                            text = "#${tag.name}",
                                                            style = MaterialTheme.typography.labelMedium.copy(
                                                                fontWeight = FontWeight.Medium,
                                                            ),
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        )

                                                        Surface(
                                                            onClick = { viewModel.handleAction(FeedAction.TagRemoved(tag)) },
                                                            shape = CircleShape,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                                            modifier = Modifier.size(20.dp),
                                                        ) {
                                                            Box(
                                                                contentAlignment = Alignment.Center,
                                                                modifier = Modifier.fillMaxSize(),
                                                            ) {
                                                                Text(
                                                                    text = "×",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                    fontWeight = FontWeight.Bold,
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
                        }

                        // Suggested tags section
                        if (currentState.searchQuery.isBlank() && currentState.suggestedTags.isNotEmpty()) {
                            item("suggested_tags") {
                                Column(
                                    modifier = Modifier.padding(bottom = SpacingTokens.md),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    shape = CircleShape,
                                                ),
                                        )
                                        Text(
                                            text = "Trending tags",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
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
                                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Chip.spacing),
                                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Chip.spacing),
                                    ) {
                                        currentState.suggestedTags.take(8).forEach { tag ->
                                            Surface(
                                                onClick = { viewModel.handleAction(FeedAction.TagSelected(tag)) },
                                                shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                ),
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(
                                                        horizontal = SpacingTokens.Chip.horizontalPadding,
                                                        vertical = SpacingTokens.sm + 2.dp,
                                                    ),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(
                                                                color = MaterialTheme.colorScheme.secondary,
                                                                shape = CircleShape,
                                                            ),
                                                    )

                                                    Text(
                                                        text = "#${tag.name}",
                                                        style = MaterialTheme.typography.labelLarge.copy(
                                                            fontWeight = FontWeight.Medium,
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Posts
                        items(
                            items = currentState.posts,
                            key = { it.id.value },
                        ) { post ->
                            PostCard(
                                post = post,
                                onLikeClick = { postId ->
                                    if (post.isUpvotedByCurrentUser) {
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
                                    viewModel.handleAction(FeedAction.UserClicked(username))
                                },
                                onTagClick = { tag ->
                                    viewModel.handleAction(FeedAction.TagSelected(tag))
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

            is FeedUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = SpacingTokens.md),
                    contentAlignment = Alignment.Center,
                ) {
                    QodeActionErrorCard(
                        message = currentState.errorType.toLocalizedMessage(),
                        errorAction = if (currentState.isRetryable) ErrorAction.RETRY else ErrorAction.DISMISS_ONLY,
                        onActionClicked = { viewModel.handleAction(FeedAction.RetryClicked) },
                        onDismiss = { viewModel.handleAction(FeedAction.ErrorDismissed) },
                        modifier = Modifier.fillMaxWidth(),
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
