package com.qodein.feature.post.submission

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.theme.SpacingTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostSubmissionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    viewModel: PostSubmissionViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "PostSubmissionScreen")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(events) {
        when (val event = events) {
            is PostSubmissionEvent.NavigateBack -> onNavigateBack()
            is PostSubmissionEvent.ShowSnackbar -> {
                // TODO: Show snackbar
            }
            is PostSubmissionEvent.OpenImagePicker -> {
                // TODO: Launch image picker
            }
            null -> { /* No event */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onAction(PostSubmissionAction.NavigateBack) }) {
                        Icon(QodeActionIcons.Back, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onAction(PostSubmissionAction.Submit) },
                        enabled = uiState.canSubmit,
                    ) {
                        Text("Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { paddingValues ->
        PostSubmissionContent(
            uiState = uiState,
            onAction = viewModel::onAction,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }

    // Auth sheet
    if (uiState.isAuthSheetVisible) {
    }
}

@Composable
private fun PostSubmissionContent(
    uiState: PostSubmissionUiState,
    onAction: (PostSubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(SpacingTokens.md),
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
            IconButton(
                onClick = { onAction(PostSubmissionAction.AddImage) },
                modifier = Modifier
                    .size(64.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
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
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(24.dp),
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
                    width = 2.dp,
                    color = if (errorText != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    shape = RoundedCornerShape(24.dp),
                ),
        )

        // Character counter
        Text(
            text = "$charCount/$maxChars",
            style = MaterialTheme.typography.bodySmall,
            color = if (charCount > maxChars) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 8.dp),
        )
    }
}
