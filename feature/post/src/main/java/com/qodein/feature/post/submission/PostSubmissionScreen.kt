package com.qodein.feature.post.submission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeinBasicTextField
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.component.ContentImage
import com.qodein.core.ui.component.FullScreenImageViewer
import com.qodein.core.ui.text.asString
import com.qodein.feature.post.R
import com.qodein.feature.post.submission.component.PostCreationTopBar
import com.qodein.feature.post.submission.component.PostSubmissionBottomToolbar
import com.qodein.feature.post.submission.component.TagSelector
import com.qodein.feature.post.submission.component.TagSelectorBottomSheet
import com.qodein.shared.model.Post
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostSubmissionRoute(
    onNavigateBack: () -> Unit,
    onPostSubmitted: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostSubmissionViewModel = koinViewModel()
) {
    TrackScreenViewEvent(screenName = "PostSubmissionScreen")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val imageLimitReachedText = stringResource(R.string.image_limit_reached)
    val imagesPartiallyAddedText = stringResource(R.string.images_partially_added)

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = Post.MAX_IMAGES),
    ) { uris ->
        viewModel.onAction(PostSubmissionAction.UpdateImageUris(uris.map { it.toString() }))
    }

    LaunchedEffect(Unit) {
        viewModel.events.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { event ->
            when (event) {
                is PostSubmissionEvent.NavigateBack -> onNavigateBack()
                is PostSubmissionEvent.PostSubmitted -> onPostSubmitted()
                is PostSubmissionEvent.NavigateToAuth -> onNavigateToAuth(event.action)
                is PostSubmissionEvent.ShowError -> snackbarHostState.showSnackbar(
                    message = event.message.asString(context),
                    withDismissAction = true,
                )
                is PostSubmissionEvent.ImageLimitReached -> snackbarHostState.showSnackbar(
                    message = imageLimitReachedText,
                    withDismissAction = true,
                )
                is PostSubmissionEvent.ImagesPartiallyAdded -> snackbarHostState.showSnackbar(
                    message = String.format(imagesPartiallyAddedText, event.count),
                    withDismissAction = true,
                )
                is PostSubmissionEvent.PickImagesRequested -> pickMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }
        }
    }

    PostSubmissionScreen(
        modifier = modifier,
        uiState = uiState,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        pickMediaLauncher = pickMediaLauncher,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostSubmissionScreen(
    uiState: PostSubmissionUiState,
    onAction: (PostSubmissionAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var showTagBottomSheet by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var fullScreenImageIndex by remember { mutableIntStateOf(0) }
    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .hazeSource(state = hazeState),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PostCreationTopBar(
                canSubmit = uiState.canSubmit,
                onNavigateBack = { onAction(PostSubmissionAction.NavigateBack) },
                onSubmit = { onAction(PostSubmissionAction.Submit) },
            )
        },
        bottomBar = {
            PostSubmissionBottomToolbar(
                disable = uiState.imageUris.size >= Post.MAX_IMAGES,
                onClick = { onAction(PostSubmissionAction.PickImages) },
                modifier = Modifier.navigationBarsPadding(),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            PostSubmissionContent(
                uiState = uiState,
                onAction = onAction,
                onOpenTagSelector = { showTagBottomSheet = true },
                onOpenImage = { index ->
                    focusManager.clearFocus()
                    fullScreenImageIndex = index
                    showFullScreenImage = true
                },
                modifier = Modifier.fillMaxSize(),
            )

            if (showTagBottomSheet) {
                TagSelectorBottomSheet(
                    selectedTags = uiState.tags,
                    currentTagInput = uiState.tagInput,
                    onTagChange = { onAction(PostSubmissionAction.UpdateTag(it)) },
                    onTagAdded = { onAction(PostSubmissionAction.AddTag(it)) },
                    onTagRemoved = { onAction(PostSubmissionAction.RemoveTag(it)) },
                    onDismiss = { showTagBottomSheet = false },
                )
            }
        }
    }

    if (showFullScreenImage && uiState.imageUris.isNotEmpty()) {
        FullScreenImageViewer(
            uri = uiState.imageUris[fullScreenImageIndex],
            onDismiss = { showFullScreenImage = false },
            hazeState = hazeState,
        )
    }
}

@Composable
private fun PostSubmissionContent(
    uiState: PostSubmissionUiState,
    onAction: (PostSubmissionAction) -> Unit,
    onOpenTagSelector: () -> Unit,
    onOpenImage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val contentFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding(),
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
                ContentImage(
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

@PreviewLightDark
@Composable
private fun PostSubmissionContentPreview() {
    QodeTheme {
        PostSubmissionScreen(
            uiState = PostSubmissionUiState(),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            pickMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {},
        )
    }
}
