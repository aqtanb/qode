package com.qodein.feature.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeEmptyState
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.scroll.RegisterScrollState
import com.qodein.core.ui.scroll.ScrollStateRegistry
import com.qodein.feature.feed.component.PostCard
import com.qodein.feature.feed.component.SearchBar
import com.qodein.feature.feed.preview.MockFeedData

// MARK: - Constants

private object FeedScreenConstants {
    const val SCREEN_NAME = "Feed"
    const val PAGINATION_LOAD_THRESHOLD = 3
}

// MARK: - Main Screen

/**
 * Modern feed screen with post feed and tag-based filtering
 * Follows the Screen + Content pattern for better organization
 */
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
    scrollStateRegistry: ScrollStateRegistry? = null
) {
    TrackScreenViewEvent(screenName = FeedScreenConstants.SCREEN_NAME)

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()

    // Register scroll state for bottom navigation auto-hiding
    scrollStateRegistry?.RegisterScrollState(lazyListState)

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
            lastVisibleItemIndex > (totalItemsNumber - FeedScreenConstants.PAGINATION_LOAD_THRESHOLD) &&
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

    // Use the Screen + Content pattern like other screens
    FeedContent(
        uiState = uiState,
        listState = lazyListState,
        onAction = viewModel::handleAction,
        modifier = modifier,
    )
}

// MARK: - Content

@Composable
private fun FeedContent(
    uiState: FeedUiState,
    listState: LazyListState,
    onAction: (FeedAction) -> Unit,
    modifier: Modifier = Modifier
) {
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

            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { onAction(FeedAction.FeedQueryChanged(it)) },
                onSearchSubmit = { onAction(FeedAction.FeedSubmitted) },
                onClearSearch = { onAction(FeedAction.ClearFeed) },
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))
        }

        // Content based on state
        when (uiState) {
            is FeedUiState.Loading -> {
                FeedLoadingState()
            }
            is FeedUiState.Content -> {
                if (uiState.isEmpty) {
                    FeedEmptyState(
                        hasFilters = uiState.hasFilters,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    FeedPostsList(
                        uiState = uiState,
                        listState = listState,
                        onAction = onAction,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            is FeedUiState.Error -> {
                FeedErrorState(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

// MARK: - State Components

@Composable
private fun FeedLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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

@Composable
private fun FeedEmptyState(
    hasFilters: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.md),
        contentAlignment = Alignment.Center,
    ) {
        QodeEmptyState(
            icon = Icons.Default.Search,
            title = if (hasFilters) "No posts found" else "Start exploring",
            description = if (hasFilters) {
                "Try adjusting your search or tags to find more posts."
            } else {
                "Search for posts by tags or content to discover what the community is talking about!"
            },
        )
    }
}

@Composable
private fun FeedErrorState(
    uiState: FeedUiState.Error,
    onAction: (FeedAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.md),
        contentAlignment = Alignment.Center,
    ) {
        QodeErrorCard(
            error = uiState.errorType,
            onRetry = { onAction(FeedAction.RetryClicked) },
            onDismiss = { onAction(FeedAction.ErrorDismissed) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FeedPostsList(
    uiState: FeedUiState.Content,
    listState: LazyListState,
    onAction: (FeedAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        contentPadding = PaddingValues(
            start = SpacingTokens.md,
            end = SpacingTokens.md,
            bottom = SpacingTokens.xxl,
        ),
    ) {
        // Posts
        items(
            items = uiState.posts,
            key = { it.id.value },
        ) { post ->
            PostCard(
                post = post,
                onLikeClick = { postId ->
                    if (post.isUpvotedByCurrentUser) {
                        onAction(FeedAction.PostUnliked(postId))
                    } else {
                        onAction(FeedAction.PostLiked(postId))
                    }
                },
                onCommentClick = { postId ->
                    onAction(FeedAction.PostCommentClicked(postId))
                },
                onShareClick = { postId ->
                    onAction(FeedAction.PostShared(postId))
                },
                onUserClick = { username ->
                    onAction(FeedAction.UserClicked(username))
                },
                onTagClick = { tag ->
                    onAction(FeedAction.TagSelected(tag))
                },
            )
        }

        // Loading more indicator
        if (uiState.isLoadingMore) {
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

// MARK: - Previews

@Preview(name = "Feed Screen - Loading", showBackground = true)
@Composable
private fun FeedScreenLoadingPreview() {
    QodeTheme {
        FeedContent(
            uiState = MockFeedData.createLoadingState(),
            listState = rememberLazyListState(),
            onAction = {},
        )
    }
}

@Preview(name = "Feed Screen - Content with Posts", showBackground = true)
@Composable
private fun FeedScreenContentPreview() {
    QodeTheme {
        FeedContent(
            uiState = MockFeedData.createContentState(),
            listState = rememberLazyListState(),
            onAction = {},
        )
    }
}

@Preview(name = "Feed Screen - Content with Filters", showBackground = true)
@Composable
private fun FeedScreenWithFiltersPreview() {
    QodeTheme {
        FeedContent(
            uiState = MockFeedData.createContentWithFiltersState(),
            listState = rememberLazyListState(),
            onAction = {},
        )
    }
}

@Preview(name = "Feed Screen - Empty with Filters", showBackground = true)
@Composable
private fun FeedScreenEmptyWithFiltersPreview() {
    QodeTheme {
        FeedContent(
            uiState = MockFeedData.createEmptyState(),
            listState = rememberLazyListState(),
            onAction = {},
        )
    }
}

@Preview(name = "Feed Screen - Empty No Filters", showBackground = true)
@Composable
private fun FeedScreenEmptyNoFiltersPreview() {
    QodeTheme {
        FeedContent(
            uiState = MockFeedData.createNoFiltersEmptyState(),
            listState = rememberLazyListState(),
            onAction = {},
        )
    }
}

@Preview(name = "Feed Screen - Error", showBackground = true)
@Composable
private fun FeedScreenErrorPreview() {
    QodeTheme {
        FeedContent(
            uiState = MockFeedData.createErrorState(),
            listState = rememberLazyListState(),
            onAction = {},
        )
    }
}

@Preview(name = "Feed Screen - Loading More", showBackground = true)
@Composable
private fun FeedScreenLoadingMorePreview() {
    QodeTheme {
        FeedContent(
            uiState = MockFeedData.createLoadingMoreState(),
            listState = rememberLazyListState(),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun FeedScreenDarkThemePreview() {
    QodeTheme {
        FeedContent(
            uiState = MockFeedData.createContentWithFiltersState(),
            listState = rememberLazyListState(),
            onAction = {},
        )
    }
}

// Individual component previews
@Preview(name = "Feed Loading State", showBackground = true)
@Composable
private fun FeedLoadingStatePreview() {
    QodeTheme {
        FeedLoadingState()
    }
}

@Preview(name = "Feed Empty State - With Filters", showBackground = true)
@Composable
private fun FeedEmptyStateWithFiltersPreview() {
    QodeTheme {
        FeedEmptyState(hasFilters = true)
    }
}

@Preview(name = "Feed Empty State - No Filters", showBackground = true)
@Composable
private fun FeedEmptyStateNoFiltersPreview() {
    QodeTheme {
        FeedEmptyState(hasFilters = false)
    }
}
