package com.qodein.feature.post.submission

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.core.ui.component.AuthenticationBottomSheet
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.error.asUiText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostSubmissionScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: PostSubmissionViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "PostSubmissionScreen")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage = (events as? PostSubmissionEvent.ShowError)?.error?.asUiText()

    LaunchedEffect(events, errorMessage) {
        when (events) {
            is PostSubmissionEvent.NavigateBack -> onNavigateBack()
            is PostSubmissionEvent.PostSubmitted -> onNavigateBack()
            is PostSubmissionEvent.ShowError -> {
                errorMessage?.let {
                    snackbarHostState.showSnackbar(
                        message = it,
                        withDismissAction = true,
                    )
                }
            }
            is PostSubmissionEvent.OpenImagePicker -> {
                // TODO: Launch image picker
            }
            null -> { /* No event */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            when (val currentState = uiState) {
                is PostSubmissionUiState.Success -> {
                    PostSubmissionTopBar(
                        canSubmit = currentState.canSubmit,
                        onNavigateBack = { viewModel.onAction(PostSubmissionAction.NavigateBack) },
                        onSubmit = { viewModel.onAction(PostSubmissionAction.Submit) },
                    )
                }
                else -> {
                    PostSubmissionTopBar(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostSubmissionTopBar(
    canSubmit: Boolean,
    onNavigateBack: () -> Unit,
    onSubmit: () -> Unit
) {
    QodeTopAppBar(
        title = "Create Post",
        navigationIcon = QodeActionIcons.Back,
        onNavigationClick = onNavigateBack,
        customActions = {
            TextButton(
                onClick = onSubmit,
                enabled = canSubmit,
            ) {
                Text("Share")
            }
        },
    )
}

@Composable
private fun PostSubmissionContent(
    uiState: PostSubmissionUiState.Success,
    onAction: (PostSubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Title field
        CircularTextField(
            value = uiState.title,
            onValueChange = { onAction(PostSubmissionAction.UpdateTitle(it)) },
            placeholder = "What's your story?",
            charCount = uiState.titleCharCount,
            maxChars = 200,
            errorText = uiState.validationErrors.titleError,
            singleLine = true,
        )

        // Content field
        CircularTextField(
            value = uiState.content,
            onValueChange = { onAction(PostSubmissionAction.UpdateContent(it)) },
            placeholder = "Share your thoughts with the world...",
            charCount = uiState.contentCharCount,
            maxChars = 2000,
            errorText = uiState.validationErrors.contentError,
            minLines = 5,
            singleLine = false,
        )

        // Image picker button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(SpacingTokens.huge)
                    .border(ShapeTokens.Border.medium, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable(onClick = { onAction(PostSubmissionAction.AddImage) }),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    QodeCategoryIcons.Camera,
                    contentDescription = "Add image",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // Image preview row
        if (uiState.imageUris.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                contentPadding = PaddingValues(horizontal = SpacingTokens.sm),
            ) {
                itemsIndexed(uiState.imageUris) { index, uri ->
                    // TODO: Image preview with remove button
                    Text("Image $index")
                }
            }
        }

        // Tags section
        if (uiState.tags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                contentPadding = PaddingValues(horizontal = SpacingTokens.sm),
            ) {
                items(uiState.tags.size) { index ->
                    // TODO: Tag chip with remove button
                    Text("Tag ${index + 1}")
                }
            }
        }

        // Validation errors
        if (uiState.validationErrors.hasErrors) {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
                uiState.validationErrors.titleError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                uiState.validationErrors.contentError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                uiState.validationErrors.tagsError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
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

@Composable
private fun CircularTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    charCount: Int,
    maxChars: Int,
    errorText: String?,
    singleLine: Boolean,
    minLines: Int = 1,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row {
            Box {
            }
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(SpacingTokens.lg),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
            isError = errorText != null,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = ShapeTokens.Border.medium,
                    color = if (errorText != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    shape = RoundedCornerShape(SpacingTokens.lg),
                ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun PostSubmissionTopBarPreview() {
    QodeTheme {
        PostSubmissionTopBar(
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
        )
    }
}
