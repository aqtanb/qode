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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.core.ui.component.AuthenticationBottomSheet
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.error.asUiText
import com.qodein.feature.post.detail.component.PostDetailSection
import com.qodein.feature.post.detail.component.PostDetailTopAppBar
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Post
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostDetailRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var authPromptAction by remember { mutableStateOf<AuthPromptAction?>(null) }
    var errorToShow by remember { mutableStateOf<OperationError?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PostDetailEvent.ShowError -> {
                    errorToShow = event.error
                }
                is PostDetailEvent.ShowAuthPrompt -> {
                    authPromptAction = event.authPromptAction
                }
            }
        }
    }

    errorToShow?.let { error ->
        val errorMessage = error.asUiText()
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short,
                withDismissAction = true,
            )
            errorToShow = null
        }
    }

    PostDetailScreen(
        onNavigateBack = onNavigateBack,
        onAction = viewModel::onAction,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )

    authPromptAction?.let { action ->
        AuthenticationBottomSheet(
            authPromptAction = action,
            onSignInClick = {
                viewModel.onAction(PostDetailAction.SignInWithGoogleClicked(context))
            },
            onDismiss = {
                authPromptAction = null
            },
            isLoading = uiState.isSigningIn,
        )
    }
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

    Scaffold(
        topBar = {
            PostDetailTopAppBar(
                onNavigationClick = onNavigateBack,
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
