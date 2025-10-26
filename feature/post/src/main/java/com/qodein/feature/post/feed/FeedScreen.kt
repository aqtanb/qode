package com.qodein.feature.post.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.preview.PostPreviewData
import com.qodein.feature.post.component.FullScreenImageViewer
import com.qodein.feature.post.feed.component.FeedTopAppBar
import com.qodein.feature.post.feed.component.PostCard
import com.qodein.feature.post.feed.component.PostCardSkeleton
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.Post
import com.qodein.shared.model.User
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun FeedRoute(
    user: User?,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FeedScreen(
        uiState = uiState,
        user = user,
        onRetry = { viewModel.onAction(FeedAction.LoadPosts) },
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier,
    )
}

@Composable
internal fun FeedScreen(
    uiState: FeedUiState,
    user: User?,
    onRetry: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
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
                    user = user,
                    onProfileClick = onProfileClick,
                    onSettingsClick = onSettingsClick,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                when (val currentState = uiState) {
                    is FeedUiState.Loading -> FeedLoadingState()
                    is FeedUiState.Success -> {
                        FeedContent(
                            posts = currentState.posts,
                            onImageClick = { uri ->
                                focusManager.clearFocus()
                                fullScreenImageUri = uri
                                showFullScreenImage = true
                            },
                        )
                    }
                    is FeedUiState.Error -> FeedErrorState(
                        error = currentState.error,
                        onRetry = onRetry,
                    )
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
private fun FeedContent(
    posts: List<Post>,
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
                    modifier = Modifier.padding(horizontal = SpacingTokens.md),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            PostCard(
                post = posts[index],
                onPostClick = { },
                onImageClick = onImageClick,
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

@Composable
private fun FeedErrorState(
    error: OperationError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeErrorCard(
        error = error,
        onRetry = onRetry,
        modifier = modifier,
    )
}

@ThemePreviews
@Composable
private fun FeedScreenPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FeedContent(
                posts = PostPreviewData.allPosts,
                onImageClick = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun FeedScreenLoadingStatePreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            FeedLoadingState()
        }
    }
}

@ThemePreviews
@Composable
private fun FeedScreenErrorStatePreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            FeedErrorState(
                error = SystemError.Offline,
                onRetry = {},
            )
        }
    }
}
