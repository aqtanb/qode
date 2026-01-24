package com.qodein.feature.post.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.component.FullScreenImageViewer
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.component.post.PostCard
import com.qodein.core.ui.component.post.PostCardSkeleton
import com.qodein.core.ui.navigation.PostKeys
import com.qodein.core.ui.preview.PostPreviewData
import com.qodein.feature.post.feed.component.FeedTopAppBar
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.PostId
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.androidx.compose.koinViewModel

@Composable
fun FeedRoute(
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPostClick: (PostId) -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit,
    backStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit) {
        viewModel.events.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { event ->
            when (event) {
                FeedEvent.NavigateToProfile -> onProfileClick()
                FeedEvent.NavigateToSettings -> onSettingsClick()
                FeedEvent.NavigateToAuth -> onNavigateToAuth(AuthPromptAction.Profile)
                is FeedEvent.NavigateToPost -> onPostClick(event.postId)
            }
        }
    }

    val postSubmitted by backStackEntry.savedStateHandle
        .getStateFlow(PostKeys.KEY_POST_SUBMITTED, false)
        .collectAsStateWithLifecycle()

    LaunchedEffect(postSubmitted) {
        if (postSubmitted) {
            viewModel.handleRefresh()
            backStackEntry.savedStateHandle.remove<Boolean>(PostKeys.KEY_POST_SUBMITTED)
        }
    }

    val userBlocked by backStackEntry.savedStateHandle
        .getStateFlow(PostKeys.KEY_POST_AUTHOR_BLOCKED, false)
        .collectAsStateWithLifecycle()

    LaunchedEffect(userBlocked) {
        if (userBlocked) {
            viewModel.handleRefresh()
            backStackEntry.savedStateHandle.remove<Boolean>(PostKeys.KEY_POST_AUTHOR_BLOCKED)
        }
    }

    FeedScreen(
        uiState = uiState,
        onAction = { viewModel.onAction(it) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val pullToRefreshState = rememberPullToRefreshState()

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
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = uiState.isRefreshing,
                contentAlignment = Alignment.Center,
                onRefresh = { onAction(FeedAction.RefreshData) },
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            ) {
                when (val state = uiState.postsState) {
                    is PostsUiState.Loading -> FeedLoadingState()
                    is PostsUiState.Success -> {
                        PostsContent(
                            state = state,
                            onImageClick = { uri ->
                                focusManager.clearFocus()
                                fullScreenImageUri = uri
                                showFullScreenImage = true
                            },
                            onPostClick = { postId -> onAction(FeedAction.PostClicked(postId)) },
                            onLoadMore = { onAction(FeedAction.LoadMorePosts) },
                        )
                    }
                    is PostsUiState.Error -> {
                        QodeErrorCard(
                            error = state.error,
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
    state: PostsUiState.Success,
    onPostClick: (PostId) -> Unit,
    onImageClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, state) {
        snapshotFlow {
            if (state.hasMore && !state.isLoadingMore) {
                val layoutInfo = listState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()

                if (lastVisibleItem == null || totalItems == 0) {
                    false
                } else {
                    lastVisibleItem.index >= totalItems - 1
                }
            } else {
                false
            }
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                onLoadMore()
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().padding(horizontal = SpacingTokens.xs),
        contentPadding = PaddingValues(bottom = SpacingTokens.gigantic, top = SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        items(state.posts.size) { index ->
            PostCard(
                post = state.posts[index],
                onPostClick = { onPostClick(state.posts[index].id) },
                onImageClick = onImageClick,
            )
        }

        if (state.hasMore && state.isLoadingMore) {
            item {
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
                postsState = PostsUiState.Success(
                    posts = PostPreviewData.allPosts,
                    hasMore = false,
                    nextCursor = null,
                ),
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
