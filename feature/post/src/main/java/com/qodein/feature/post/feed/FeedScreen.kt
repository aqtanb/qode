package com.qodein.feature.post.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.FullScreenImageViewer
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.component.post.PostCard
import com.qodein.core.ui.component.post.PostCardSkeleton
import com.qodein.core.ui.preview.PostPreviewData
import com.qodein.feature.post.feed.component.FeedTopAppBar
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.androidx.compose.koinViewModel

@Composable
fun FeedRoute(
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPostClick: (PostId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                FeedEvent.NavigateToProfile -> onProfileClick()
                FeedEvent.NavigateToSettings -> onSettingsClick()
                is FeedEvent.NavigateToPost -> {
                }
            }
        }
    }

    FeedScreen(
        uiState = uiState,
        onAction = { viewModel.onAction(it) },
        modifier = modifier,
    )
}

@Composable
internal fun FeedScreen(
    uiState: FeedUiState,
    onAction: (FeedAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var fullScreenImageUri by remember { mutableStateOf("") }
    val hazeState = remember { HazeState() }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            topBar = {
                FeedTopAppBar(
                    user = uiState.currentUser,
                    onProfileClick = { onAction(FeedAction.ProfileClicked) },
                    onSettingsClick = { onAction(FeedAction.SettingsClicked) },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                when (uiState.postsState) {
                    is PostsUiState.Loading -> FeedLoadingState()
                    is PostsUiState.Success -> {
                        PostsContent(
                            posts = uiState.postsState.posts,
                            onImageClick = { uri ->
                                focusManager.clearFocus()
                                fullScreenImageUri = uri
                                showFullScreenImage = true
                            },
                            onPostClick = { postId -> onAction(FeedAction.PostClicked(postId)) },
                        )
                    }
                    is PostsUiState.Error -> {
                        QodeErrorCard(
                            error = uiState.postsState.error,
                            onRetry = { onAction(FeedAction.RetryClicked) },
                        )
                    }
                }
            }
        }

        if (showFullScreenImage) {
            FullScreenImageViewer(
                uri = fullScreenImageUri,
                onDismiss = { showFullScreenImage = false },
                hazeState = hazeState,
            )
        }
    }
}

@Composable
private fun PostsContent(
    posts: List<Post>,
    onPostClick: (PostId) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = SpacingTokens.gigantic),
    ) {
        items(posts.size) { index ->
            if (index < posts.size) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = SpacingTokens.sm),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            PostCard(
                post = posts[index],
                onPostClick = { onPostClick(posts[index].id) },
                onImageClick = onImageClick,
                modifier = Modifier.padding(horizontal = SpacingTokens.sm),
            )
        }
    }
}

@Composable
private fun FeedLoadingState(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.sm),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        items(3) { index ->
            PostCardSkeleton(
                showImage = index % 2 == 0,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PostsSuccessPreview() {
    QodeTheme {
        FeedScreen(
            uiState = FeedUiState(
                postsState = PostsUiState.Success(posts = PostPreviewData.allPosts),
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PostsLoadingPreview() {
    QodeTheme {
        FeedScreen(
            uiState = FeedUiState(),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PostsErrorPreview() {
    QodeTheme {
        FeedScreen(
            uiState = FeedUiState(postsState = PostsUiState.Error(SystemError.Unknown)),
            onAction = {},
        )
    }
}
