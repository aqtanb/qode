package com.qodein.feature.post.detail

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.feature.post.detail.component.PostDetailSection
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId

@Composable
internal fun PostDetailRoute(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PostDetailScreen(
        isDarkTheme = isDarkTheme,
        uiState = uiState,
    )
}

@Composable
private fun PostDetailScreen(
    isDarkTheme: Boolean,
    uiState: PostDetailUiState,
    modifier: Modifier = Modifier
) {
    TrackScreenViewEvent(screenName = "Post Detail")
    when (val postState = uiState.postState) {
        is DataState.Error -> PostDetailErrorState(
            error = postState.error,
            onRetry = {},
            modifier = modifier,
        )
        DataState.Loading -> CircularProgressIndicator()
        is DataState.Success -> PostDetailSuccessState(
            post = postState.data,
            onUpvoteClick = { },
            onDownvoteClick = { },
            onCommentClick = { },
            onShareClick = { },
            onImageClick = {},
        )
    }
}

@Composable
private fun PostDetailSuccessState(
    post: Post,
    onUpvoteClick: (PostId) -> Unit,
    onDownvoteClick: (PostId) -> Unit,
    onCommentClick: (PostId) -> Unit,
    onShareClick: (PostId) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PostDetailSection(
        post = post,
        onUpvoteClick = { },
        onDownvoteClick = { },
        onCommentClick = { },
        onShareClick = { },
        onImageClick = onImageClick,
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
