package com.qodein.feature.post.submission

import android.R.id.message
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.TextUnit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeinBasicTextField
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.core.ui.component.AuthenticationBottomSheet
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.error.asUiText
import com.qodein.feature.post.R
import com.qodein.feature.post.component.FullScreenImageViewer
import com.qodein.feature.post.component.PostImage
import com.qodein.feature.post.submission.component.PostCreationTopBar
import com.qodein.feature.post.submission.component.PostSubmissionBottomToolbar
import com.qodein.feature.post.submission.component.TagSelector
import com.qodein.feature.post.submission.component.TagSelectorBottomSheet
import com.qodein.shared.common.error.OperationError
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostSubmissionScreen(
    onNavigateBack: () -> Unit,
    viewModel: PostSubmissionViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "PostSubmissionScreen")

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showTagBottomSheet by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var fullScreenImageIndex by remember { mutableStateOf(0) }
    val hazeState = remember { HazeState() }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
    ) { uris ->
        if (uris.isNotEmpty()) {
            val currentState = uiState as? PostSubmissionUiState.Success ?: return@rememberLauncherForActivityResult
            val currentCount = currentState.imageUris.size
            val availableSlots = 5 - currentCount
            val urisToAdd = uris.take(availableSlots)

            if (uris.size > availableSlots) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.images_partially_added, urisToAdd.size),
                        withDismissAction = true,
                    )
                }
            }

            viewModel.onAction(PostSubmissionAction.UpdateImageUris(urisToAdd.map { it.toString() }))
        }
    }

    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = null)
    val errorMessage = (events as? PostSubmissionEvent.ShowError)?.error?.asUiText()

    LaunchedEffect(events, errorMessage) {
        when (events) {
            is PostSubmissionEvent.NavigateBack -> onNavigateBack()
            is PostSubmissionEvent.PostSubmitted -> onNavigateBack()
            is PostSubmissionEvent.ShowError -> {
                errorMessage?.let { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        withDismissAction = true,
                    )
                }
            }
            null -> { /* No event yet */ }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                when (val currentState = uiState) {
                    is PostSubmissionUiState.Success -> {
                        PostCreationTopBar(
                            canSubmit = currentState.canSubmit,
                            onNavigateBack = { viewModel.onAction(PostSubmissionAction.NavigateBack) },
                            onSubmit = { viewModel.onAction(PostSubmissionAction.Submit) },
                        )
                    }
                    else -> {
                        PostCreationTopBar(
                            canSubmit = false,
                            onNavigateBack = { viewModel.onAction(PostSubmissionAction.NavigateBack) },
                            onSubmit = {},
                        )
                    }
                }
            },
            bottomBar = {
                val currentState = uiState as? PostSubmissionUiState.Success
                if (currentState != null && currentState.authentication is PostAuthenticationState.Authenticated) {
                    PostSubmissionBottomToolbar(
                        isImageLimitReached = currentState.imageUris.size >= 5,
                        onClick = {
                            if (currentState.imageUris.size >= 5) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.image_limit_reached),
                                        withDismissAction = true,
                                    )
                                }
                            } else {
                                pickMediaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            }
                        },
                        modifier = Modifier.imePadding().navigationBarsPadding(),
                    )
                }
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            ) {
                when (val currentState = uiState) {
                    is PostSubmissionUiState.Loading -> {
                        LoadingState()
                    }
                    is PostSubmissionUiState.Success -> {
                        val showAuthenticationSheet = currentState.authentication !is PostAuthenticationState.Authenticated
                        if (showAuthenticationSheet) {
                            val isSigningIn = currentState.authentication is PostAuthenticationState.Loading
                            AuthenticationBottomSheet(
                                authPromptAction = AuthPromptAction.CreatePost,
                                onSignInClick = { viewModel.onAction(PostSubmissionAction.SignInWithGoogle(context)) },
                                onDismiss = { viewModel.onAction(PostSubmissionAction.DismissAuthSheet) },
                                isLoading = isSigningIn,
                            )
                        } else {
                            PostSubmissionContent(
                                uiState = currentState,
                                onAction = viewModel::onAction,
                                onOpenTagSelector = { showTagBottomSheet = true },
                                onOpenImagePicker = {
                                    if (currentState.imageUris.size >= 5) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = context.getString(R.string.image_limit_reached),
                                                withDismissAction = true,
                                            )
                                        }
                                    } else {
                                        pickMediaLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                        )
                                    }
                                },
                                onOpenImage = { index ->
                                    focusManager.clearFocus()
                                    fullScreenImageIndex = index
                                    showFullScreenImage = true
                                },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        if (currentState.compression is ImageCompressionState.Compressing) {
                            LaunchedEffect(currentState.compression) {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(
                                        R.string.compressing_images,
                                        currentState.compression.current,
                                        currentState.compression.total,
                                    ),
                                    withDismissAction = false,
                                    duration = androidx.compose.material3.SnackbarDuration.Indefinite,
                                )
                            }
                        }

                        if (showTagBottomSheet) {
                            TagSelectorBottomSheet(
                                selectedTags = currentState.tags,
                                onTagSelected = { viewModel.onAction(PostSubmissionAction.AddTag(it)) },
                                onTagRemoved = { viewModel.onAction(PostSubmissionAction.RemoveTag(it)) },
                                onDismiss = { showTagBottomSheet = false },
                                popularTags = listOf(
                                    "tech",
                                    "food",
                                    "travel",
                                    "lifestyle",
                                    "fashion",
                                    "gaming",
                                    "music",
                                    "sports",
                                    "fitness",
                                    "beauty",
                                ),
                            )
                        }
                    }
                    is PostSubmissionUiState.Error -> {
                        ErrorState(
                            error = currentState.errorType,
                            onRetry = { viewModel.onAction(PostSubmissionAction.RetryPostSubmission) },
                        )
                    }
                }
            }
        }

        if (showFullScreenImage) {
            val currentState = uiState as? PostSubmissionUiState.Success
            if (currentState != null && currentState.imageUris.isNotEmpty()) {
                FullScreenImageViewer(
                    uri = currentState.imageUris[fullScreenImageIndex],
                    onDismiss = { showFullScreenImage = false },
                    hazeState = hazeState,
                )
            }
        }
    }
}

@Composable
private fun PostSubmissionContent(
    uiState: PostSubmissionUiState.Success,
    onAction: (PostSubmissionAction) -> Unit,
    onOpenTagSelector: () -> Unit,
    onOpenImagePicker: () -> Unit,
    onOpenImage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val contentFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        TagSelector(
            selectedTags = uiState.tags,
            onClick = onOpenTagSelector,
        )

        QodeinBasicTextField(
            value = uiState.title,
            onValueChange = { onAction(PostSubmissionAction.UpdateTitle(it)) },
            placeholder = stringResource(R.string.placeholder_title),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = TextUnit.Unspecified,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = SpacingTokens.md, top = SpacingTokens.md, end = SpacingTokens.md),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Sentences,
            ),
            keyboardActions = KeyboardActions(
                onNext = { contentFocusRequester.requestFocus() },
            ),
        )

        if (uiState.imageUris.isNotEmpty()) {
            HorizontalPager(
                state = rememberPagerState { uiState.imageUris.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SpacingTokens.xs),
            ) { page ->
                PostImage(
                    uri = uiState.imageUris[page],
                    currentPage = page + 1,
                    totalPages = uiState.imageUris.size,
                    onRemove = { onAction(PostSubmissionAction.RemoveImage(page)) },
                    ratio = 1f,
                    onClick = { onOpenImage(page) },
                )
            }
        }

        QodeinBasicTextField(
            value = uiState.content,
            onValueChange = { onAction(PostSubmissionAction.UpdateContent(it)) },
            placeholder = stringResource(R.string.placeholder_description),
            singleLine = false,
            minLines = 3,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.md),
            focusRequester = contentFocusRequester,
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    error: OperationError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        QodeErrorCard(
            error = error,
            onRetry = onRetry,
            onDismiss = {},
        )
    }
}

@ThemePreviews
@Composable
private fun PostCreationTopBarPreview() {
    QodeTheme {
        PostCreationTopBar(
            canSubmit = false,
            onNavigateBack = {},
            onSubmit = {},
        )
    }
}

@ThemePreviews
@Composable
private fun PostSubmissionContentPreview() {
    QodeTheme {
        PostSubmissionContent(
            uiState = PostSubmissionUiState.Success.initial().copy(imageUris = listOf("", "")),
            onAction = {},
            onOpenTagSelector = {},
            onOpenImagePicker = {},
            onOpenImage = {},
        )
    }
}
