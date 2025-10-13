package com.qodein.feature.post.submission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.core.ui.component.AuthenticationBottomSheet
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.error.asUiText
import com.qodein.feature.post.R
import com.qodein.feature.post.submission.component.PostCreationTopBar
import com.qodein.feature.post.submission.component.TagSelector
import com.qodein.feature.post.submission.component.TagSelectorBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostSubmissionScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: PostSubmissionViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "PostSubmissionScreen")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTagBottomSheet by remember { mutableStateOf(false) }

    // Image picker launcher
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
    ) { uris ->
        if (uris.isNotEmpty()) {
            val uriStrings = uris.map { it.toString() }
            viewModel.onAction(PostSubmissionAction.UpdateImageUris(uriStrings))
        }
    }

    // Collect events and map error message in Composable context
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = null)
    val errorMessage = (events as? PostSubmissionEvent.ShowError)?.error?.asUiText()

    // Handle events
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
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
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
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
                            onSignInClick = { viewModel.onAction(PostSubmissionAction.SignInWithGoogle) },
                            onDismiss = { viewModel.onAction(PostSubmissionAction.DismissAuthSheet) },
                            isDarkTheme = isDarkTheme,
                            isLoading = isSigningIn,
                        )
                    } else {
                        PostSubmissionContent(
                            uiState = currentState,
                            onAction = viewModel::onAction,
                            onOpenTagSelector = { showTagBottomSheet = true },
                            onOpenImagePicker = {
                                pickMediaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                        )
                    }

                    // Tag selector bottom sheet
                    if (showTagBottomSheet) {
                        TagSelectorBottomSheet(
                            selectedTags = currentState.tags,
                            onTagSelected = { viewModel.onAction(PostSubmissionAction.AddTag(it)) },
                            onTagRemoved = { viewModel.onAction(PostSubmissionAction.RemoveTag(it)) },
                            onDismiss = { showTagBottomSheet = false },
                            popularTags = listOf(
                                "#tech",
                                "#food",
                                "#travel",
                                "#lifestyle",
                                "#fashion",
                                "#gaming",
                                "#music",
                                "#sports",
                                "#fitness",
                                "#beauty",
                            ),
                        )
                    }
                }
                is PostSubmissionUiState.Error -> {
                    ErrorState(
                        message = currentState.errorType.asUiText(),
                        onRetry = { viewModel.onAction(PostSubmissionAction.RetryPostSubmission) },
                    )
                }
            }
        }
    }
}

/**
 * Reddit-style clean content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostSubmissionContent(
    uiState: PostSubmissionUiState.Success,
    onAction: (PostSubmissionAction) -> Unit,
    onOpenTagSelector: () -> Unit,
    onOpenImagePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 48.dp), // Space for bottom toolbar
        ) {
            // Tag selector button (circular, like Reddit's community selector)
            TagSelector(
                selectedTags = uiState.tags,
                onClick = onOpenTagSelector,
            )

            // Title (plain text, no border)
            PlainTextField(
                value = uiState.title,
                onValueChange = { onAction(PostSubmissionAction.UpdateTitle(it)) },
                placeholder = "Title",
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.md),
            )

            // Image carousel (if images exist)
            if (uiState.imageUris.isNotEmpty()) {
                HorizontalPager(
                    state = rememberPagerState { uiState.imageUris.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SpacingTokens.xs),
                ) { page ->
                    ImageCarouselItem(
                        uri = uiState.imageUris[page],
                        currentPage = page + 1,
                        totalPages = uiState.imageUris.size,
                        onRemove = { onAction(PostSubmissionAction.RemoveImage(page)) },
                    )
                }
            }

            // Body text (plain, no border, multiline)
            PlainTextField(
                value = uiState.content,
                onValueChange = { onAction(PostSubmissionAction.UpdateContent(it)) },
                placeholder = "Body text",
                singleLine = false,
                minLines = 3,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.md),
            )
        }

        // Bottom toolbar (fixed at bottom)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .imePadding()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.xs),
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = QodeNavigationIcons.Gallery,
                contentDescription = stringResource(R.string.cd_add_image),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onOpenImagePicker() },
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
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
    message: String,
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
            message = message,
            onRetry = onRetry,
        )
    }
}

/**
 * Action button with icon, label, and count indicator
 */
@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    maxCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    Box(
        modifier = modifier
            .background(
                color = if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(ShapeTokens.Corner.large),
            )
            .border(
                width = if (isActive) ShapeTokens.Border.medium else ShapeTokens.Border.thin,
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(ShapeTokens.Corner.large),
            )
            .clickable(onClick = onClick)
            .padding(SpacingTokens.md),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )

            // Count badge
            Box(
                modifier = Modifier
                    .background(
                        color = if (count > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        },
                        shape = CircleShape,
                    )
                    .padding(horizontal = SpacingTokens.xs, vertical = 2.dp),
            ) {
                Text(
                    text = "$count/$maxCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (count > 0) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

/**
 * Image preview card with remove button
 */
@Composable
private fun ImagePreviewCard(
    uri: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(ShapeTokens.Corner.medium),
            )
            .border(
                width = ShapeTokens.Border.thin,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(ShapeTokens.Corner.medium),
            ),
    ) {
        // TODO: Load actual image with Coil/Glide
        // For now, placeholder
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = QodeCategoryIcons.Camera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Remove button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.error, CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = QodeActionIcons.Close,
                contentDescription = stringResource(R.string.cd_remove_image),
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun PlainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = modifier.padding(vertical = SpacingTokens.xs),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
            innerTextField()
        },
        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
    )
}

@Composable
private fun ImageCarouselItem(
    uri: String,
    currentPage: Int,
    totalPages: Int,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
    ) {
        // TODO: Load actual image with Coil/Glide
        // Placeholder for now
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.huge + SpacingTokens.xl),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = QodeCategoryIcons.Camera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp),
            )
        }

        // Page indicator (top right, like Reddit)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(SpacingTokens.sm)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f), RoundedCornerShape(ShapeTokens.Corner.medium))
                .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxxs),
        ) {
            Text(
                text = "$currentPage/$totalPages",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        // Remove button (X)
        Icon(
            imageVector = QodeActionIcons.Close,
            contentDescription = stringResource(R.string.cd_remove_image),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(SpacingTokens.sm)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f), CircleShape)
                .clickable(onClick = onRemove)
                .padding(SpacingTokens.xxxs),
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@PreviewLightDark
@Composable
private fun CleanTopBarPreview() {
    QodeTheme {
        PostCreationTopBar(
            canSubmit = false,
            onNavigateBack = {},
            onSubmit = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PostSubmissionContentPreview() {
    QodeTheme {
        PostSubmissionContent(
            uiState = PostSubmissionUiState.Success.initial(),
            onAction = {},
            onOpenTagSelector = {},
            onOpenImagePicker = {},
        )
    }
}
