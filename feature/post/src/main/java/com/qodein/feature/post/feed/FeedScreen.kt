package com.qodein.feature.post.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qodein.core.data.model.PostSummaryDto
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PostPreviewData
import com.qodein.feature.post.feed.component.PostCard
import com.qodein.shared.model.Post

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            when (val currentState = uiState) {
                is FeedUiState.Loading -> FeedLoadingState(modifier = modifier.fillMaxSize())
                is FeedUiState.Success -> {
                    FeedContent(
                        posts = currentState.posts,
                    )
                }
                is FeedUiState.Error -> FeedErrorState(
                    onRetry = { viewModel.onAction(FeedAction.LoadPosts) },
                    modifier = modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun FeedContent(
    posts: List<Post>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = SpacingTokens.sm),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        items(posts.size) {
            PostCard(
                post = posts[it],
                onPostClick = { },
            )
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
        verticalArrangement = Arrangement.Center,
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
private fun Post.toSummaryDto(): PostSummaryDto =
    PostSummaryDto(
        id = id.value,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        title = title,
        contentPreview = content?.length?.let { if (it > 200) content?.take(200) + "..." else content },
        imageUrls = imageUrls,
        tags = tags.map { it.value },
        upvotes = upvotes,
        downvotes = downvotes,
        commentCount = commentCount,
        voteScore = upvotes - downvotes,
        userVoteState = "NONE", // Will be determined by user interaction state
    )

@PreviewLightDark
@Composable
private fun FeedScreenPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FeedContent(
                posts = PostPreviewData.allPosts,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun FeedScreenLoadingStatePreview() {
    QodeTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FeedLoadingState()
        }
    }
}

@PreviewLightDark
@Composable
private fun FeedScreenErrorStatePreview() {
    QodeTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FeedErrorState(onRetry = {})
        }
    }
}
