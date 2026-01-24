package com.qodein.feature.post.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.component.post.PostCard
import com.qodein.core.ui.component.post.VoteButtonGroup
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.preview.PostPreviewData
import com.qodein.core.ui.text.asString
import com.qodein.feature.post.R
import com.qodein.feature.post.detail.component.PostDetailTopAppBar
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PostDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit,
    onNavigateToReport: (String, String, String?) -> Unit,
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

    val title = when (val state = uiState.postState) {
        is PostUiState.Success -> state.post.title
        else -> ""
    }
    val postId = (uiState.postState as? PostUiState.Success)?.post?.id
    val authorId = (uiState.postState as? PostUiState.Success)?.post?.authorId

    Scaffold(
        topBar = {
            PostDetailTopAppBar(
                title = title,
                postId = postId,
                currentUserId = uiState.currentUserId,
                authorId = authorId,
                onNavigateBack = onNavigateBack,
                onBlockUserClick = { userId -> onAction(PostDetailAction.BlockUserClicked(userId)) },
                onReportPostClick = { postId -> onAction(PostDetailAction.ReportPostClicked(postId.value)) },
            )
        },
        modifier = modifier,
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
                onAction = onAction,
                userVoteState = uiState.userVoteState,
                userId = uiState.currentUserId,
                onImageClick = {},
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun PostDetailSuccessState(
    post: Post,
    onAction: (PostDetailAction) -> Unit,
    userVoteState: VoteState,
    userId: UserId?,
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
        )

        PostInteractionsRow(
            postId = post.id,
            upvotes = post.upvotes,
            downvotes = post.downvotes,
            userVoteState = userVoteState,
            userId = userId,
            onAction = onAction,
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PostInteractionsRow(
    postId: PostId,
    upvotes: Int,
    downvotes: Int,
    userVoteState: VoteState,
    userId: UserId?,
    onAction: (PostDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val upvoteContentDescription = stringResource(R.string.cd_upvote, upvotes)
    val downvoteContentDescription = stringResource(R.string.cd_downvote, downvotes)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var voteCount by remember { mutableIntStateOf(0) }
        var mockVoteState by remember { mutableStateOf(VoteState.NONE) }

        VoteButtonGroup(
            voteCount = voteCount,
            voteState = mockVoteState,
            onUpvote = {
                when (mockVoteState) {
                    VoteState.UPVOTE -> {
                        mockVoteState = VoteState.NONE
                        voteCount--
                    }
                    VoteState.DOWNVOTE -> {
                        mockVoteState = VoteState.UPVOTE
                        voteCount += 2
                    }
                    VoteState.NONE -> {
                        mockVoteState = VoteState.UPVOTE
                        voteCount++
                    }
                }
            },
            onDownvote = {
                when (mockVoteState) {
                    VoteState.UPVOTE -> {
                        mockVoteState = VoteState.DOWNVOTE
                        voteCount -= 2
                    }
                    VoteState.DOWNVOTE -> {
                        mockVoteState = VoteState.NONE
                        voteCount++
                    }
                    VoteState.NONE -> {
                        mockVoteState = VoteState.DOWNVOTE
                        voteCount--
                    }
                }
            },
        )

        Spacer(modifier = Modifier.weight(1f))

        FilledTonalIconButton(
            onClick = { /* TODO: Share functionality */ },
        ) {
            Icon(
                imageVector = ActionIcons.Share,
                contentDescription = "Share post",
            )
        }
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
