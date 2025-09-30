package com.qodein.feature.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.scroll.RegisterScrollState
import com.qodein.core.ui.scroll.ScrollStateRegistry
import com.qodein.feature.feed.component.FeedSearchBar
import com.qodein.feature.feed.component.PostCard
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    scrollStateRegistry: ScrollStateRegistry? = null,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

    // Register scroll state for bottom navigation auto-hiding
    scrollStateRegistry?.RegisterScrollState(lazyListState)

    // Handle error messages
    LaunchedEffect(uiState) {
        if (uiState is FeedUiState.Error && uiState.shouldShowSnackbar) {
            snackbarHostState.showSnackbar(message = "Failed to load posts")
            viewModel.onAction(FeedAction.ErrorDismissed)
        }
    }

    // Handle pagination
    LaunchedEffect(lazyListState, uiState) {
        val shouldLoadMore by derivedStateOf {
            if (uiState is FeedUiState.Content && uiState.hasMorePosts && !uiState.isLoadingMore) {
                val layoutInfo = lazyListState.layoutInfo
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = layoutInfo.totalItemsCount
                lastVisibleIndex >= totalItems - 3
            } else {
                false
            }
        }

        if (shouldLoadMore) {
            viewModel.onAction(FeedAction.LoadMorePosts)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (uiState) {
                is FeedUiState.Loading -> {
                    FeedLoadingState(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is FeedUiState.Error -> {
                    FeedErrorState(
                        onRetry = { viewModel.onAction(FeedAction.RetryClicked) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is FeedUiState.Content -> {
                    if (uiState.isEmpty && !uiState.isRefreshing) {
                        FeedEmptyState(
                            searchQuery = uiState.searchQuery,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = SpacingTokens.md,
                                end = SpacingTokens.md,
                                bottom = SpacingTokens.xl,
                            ),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
                        ) {
                            // Search Bar
                            item {
                                FeedSearchBar(
                                    query = uiState.searchQuery,
                                    onQueryChange = { viewModel.onAction(FeedAction.FeedQueryChanged(it)) },
                                    onSearchClick = { viewModel.onAction(FeedAction.FeedSubmitted) },
                                    onFilterClick = { /* TODO: Implement filters */ },
                                    modifier = Modifier.padding(bottom = SpacingTokens.sm),
                                )
                            }

                            // Posts
                            items(
                                items = uiState.posts,
                                key = { post -> post.id.value },
                            ) { post ->
                                PostCard(
                                    post = post.toSummaryDto(),
                                    onPostClick = { viewModel.onAction(FeedAction.PostClicked(PostId(it))) },
                                    onCommentClick = { viewModel.onAction(FeedAction.PostCommentClicked(PostId(it))) },
                                    onShareClick = { viewModel.onAction(FeedAction.PostShared(PostId(it))) },
                                )
                            }

                            // Loading more indicator
                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(SpacingTokens.lg),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.padding(SpacingTokens.md),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Gradient overlay at top for search bar elevation effect
            if (uiState is FeedUiState.Content && uiState.hasContent) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingTokens.md)
                        .padding(top = SpacingTokens.sm),
                ) {
                    if (lazyListState.firstVisibleItemScrollOffset > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.background,
                                            Color.Transparent,
                                        ),
                                        endY = 40f,
                                    ),
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Loading posts...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FeedErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(SpacingTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        Text(
            text = "Failed to load posts",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Please check your connection and try again",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        TextButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = SpacingTokens.sm),
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun FeedEmptyState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(SpacingTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        Text(
            text = if (searchQuery.isNotEmpty()) {
                "No posts found for \"$searchQuery\""
            } else {
                "No posts available"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Text(
            text = if (searchQuery.isNotEmpty()) {
                "Try adjusting your search terms or check back later for new posts."
            } else {
                "Be the first to share something interesting with the community!"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// Extension function to convert domain Post to PostSummaryDto for PostCard
private fun Post.toSummaryDto(): com.qodein.core.data.model.PostSummaryDto =
    com.qodein.core.data.model.PostSummaryDto(
        id = id.value,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        title = title,
        contentPreview = if (content.length > 200) content.take(200) + "..." else content,
        imageUrls = imageUrls,
        tags = tags.map { it.value },
        upvotes = upvotes,
        downvotes = downvotes,
        commentCount = commentCount,
        voteScore = upvotes - downvotes,
        createdAt = null, // Will be handled by mapper with proper time formatting
        userVoteState = "NONE", // Will be determined by user interaction state
    )

@Preview(showBackground = true)
@Composable
private fun FeedScreenPreview() {
    QodeTheme {
        // Preview with mock ViewModel would go here
        FeedLoadingState()
    }
}
