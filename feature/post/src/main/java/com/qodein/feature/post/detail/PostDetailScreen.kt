package com.qodein.feature.post.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.text.asString
import com.qodein.feature.post.detail.component.PostDetailSection
import com.qodein.feature.post.detail.component.PostDetailTopAppBar
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Post
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    TrackScreenViewEvent(screenName = "Post Detail")

    val title = when (val state = uiState.postState) {
        is DataState.Success -> state.data.title
        else -> ""
    }
    val postId = (uiState.postState as? DataState.Success)?.data?.id
    val authorId = (uiState.postState as? DataState.Success)?.data?.authorId

    Scaffold(
        topBar = {
            PostDetailTopAppBar(
                title = title,
                postId = postId,
                currentUserId = uiState.userId,
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
            is DataState.Error -> PostDetailErrorState(
                error = postState.error,
                onRetry = {},
                modifier = Modifier.padding(paddingValues),
            )
            DataState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            is DataState.Success -> PostDetailSuccessState(
                post = postState.data,
                onAction = onAction,
                userVoteState = uiState.userVoteState,
                userId = uiState.userId,
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
    PostDetailSection(
        post = post,
        onAction = onAction,
        userVoteState = userVoteState,
        userId = userId,
        onImageClick = onImageClick,
        modifier = modifier,
    )
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
