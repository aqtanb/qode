package com.qodein.feature.post.detail

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.component.FullScreenImageViewer
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.component.post.InteractionsRow
import com.qodein.core.ui.component.post.PostCard
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.preview.PostPreviewData
import com.qodein.core.ui.text.asString
import com.qodein.feature.post.detail.component.PostDetailTopAppBar
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.androidx.compose.koinViewModel
import com.qodein.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PostDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit,
    onNavigateToReport: (PostId, String, String?) -> Unit,
    onNavigateToBlockUser: (UserId, String?, String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val localContext = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { event ->
            when (event) {
                is PostDetailEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.error.toUiText().asString(localContext),
                        duration = SnackbarDuration.Short,
                    )
                }
                is PostDetailEvent.NavigateToAuth -> {
                    onNavigateToAuth(event.action)
                }

                is PostDetailEvent.NavigateToBlockUser -> onNavigateToBlockUser(
                    event.userId,
                    event.username,
                    event.photoUrl,
                )
                is PostDetailEvent.NavigateToReport -> onNavigateToReport(
                    event.reportedItemId,
                    event.itemTitle,
                    event.itemAuthor,
                )
                is PostDetailEvent.SharePost -> {
                    sharePost(localContext, event.shareContent)
                }
            }
        }
    }

    PostDetailScreen(
        onNavigateBack = onNavigateBack,
        onAction = viewModel::onAction,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
private fun PostDetailScreen(
    onNavigateBack: () -> Unit,
    onAction: (PostDetailAction) -> Unit,
    uiState: PostDetailUiState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    TrackScreenViewEvent(screenName = "Post Detail")

    var showFullScreenImage by remember { mutableStateOf(false) }
    var fullScreenImageUri by remember { mutableStateOf("") }
    val hazeState = remember { HazeState() }

    val title = when (val state = uiState.postState) {
        is PostUiState.Success -> state.post.title
        else -> ""
    }
    val postId = (uiState.postState as? PostUiState.Success)?.post?.id
    val authorId = (uiState.postState as? PostUiState.Success)?.post?.authorId

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                PostDetailTopAppBar(
                    title = title,
                    postId = postId,
                    currentUserId = uiState.currentUserId,
                    authorId = authorId,
                    onNavigateBack = onNavigateBack,
                    onBlockUserClick = { onAction(PostDetailAction.BlockUserClicked) },
                    onReportPostClick = { onAction(PostDetailAction.ReportPostClicked) },
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            when (val postState = uiState.postState) {
                is PostUiState.Error -> PostDetailErrorState(
                    error = postState.error,
                    onRetry = {},
                    modifier = Modifier.padding(paddingValues),
                )
                PostUiState.Loading -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                is PostUiState.Success -> PostDetailSuccessState(
                    post = postState.post,
                    userVoteState = uiState.userVoteState,
                    voteScoreDelta = uiState.voteScoreDelta,
                    onAction = onAction,
                    onImageClick = { uri ->
                        fullScreenImageUri = uri
                        showFullScreenImage = true
                    },
                    modifier = Modifier.padding(paddingValues),
                )
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
private fun PostDetailSuccessState(
    post: Post,
    userVoteState: VoteState,
    voteScoreDelta: Int,
    onAction: (PostDetailAction) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.xs),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        PostCard(
            post = post,
            onPostClick = {},
            onImageClick = onImageClick,
            voteScore = post.voteScore + voteScoreDelta,
        )

        InteractionsRow(
            voteState = userVoteState,
            onUpvote = { onAction(PostDetailAction.ToggleVoteClicked(VoteState.UPVOTE)) },
            onDownvote = { onAction(PostDetailAction.ToggleVoteClicked(VoteState.DOWNVOTE)) },
            onShare = { onAction(PostDetailAction.SharePostClicked) },
        )
    }
}

@Composable
private fun PostDetailErrorState(
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

private fun sharePost(
    context: Context,
    shareContent: com.qodein.shared.model.ShareContent
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, shareContent.title)
        putExtra(Intent.EXTRA_TEXT, shareContent.text)
        putExtra(Intent.EXTRA_SUBJECT, shareContent.title)
    }

    val chooserTitle = context.getString(CoreUiR.string.ui_action_share_post)
    try {
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
    } catch (_: Exception) {
    }
}

@PreviewLightDark
@Composable
private fun PostDetailScreenPreview() {
    QodeTheme {
        PostDetailScreen(
            onNavigateBack = {},
            onAction = {},
            uiState = PostDetailUiState(
                postState = PostUiState.Success(PostPreviewData.popularPost),
            ),
        )
    }
}
