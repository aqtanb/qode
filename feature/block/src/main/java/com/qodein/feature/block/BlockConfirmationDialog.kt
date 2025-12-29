package com.qodein.feature.block

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeOutlinedButton
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.text.asString
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.qodein.core.ui.R as CoreUiR

@Composable
fun BlockConfirmationDialog(
    userId: UserId,
    username: String?,
    photoUrl: String?,
    contentType: ContentType,
    onNavigateBack: () -> Unit,
    onUserBlocked: () -> Unit,
    viewModel: BlockViewModel = koinViewModel { parametersOf(userId, username, photoUrl) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is BlockUiState.Success) {
            onUserBlocked()
        }
    }

    when (val state = uiState) {
        is BlockUiState.Confirming -> {
            ConfirmationDialogContent(
                username = state.username,
                photoUrl = state.photoUrl,
                isLoading = state.isLoading,
                onConfirm = { viewModel.onAction(BlockAction.ConfirmBlock) },
                onCancel = {
                    viewModel.onAction(BlockAction.CancelBlock)
                    onNavigateBack()
                },
            )
        }
        is BlockUiState.Error -> {
            ErrorDialogContent(
                error = state.error,
                onDismiss = { viewModel.onAction(BlockAction.DismissError) },
                onCancel = onNavigateBack,
            )
        }
        is BlockUiState.Success -> {
            // Handled by LaunchedEffect
        }
    }
}

@Composable
private fun ConfirmationDialogContent(
    username: String?,
    photoUrl: String?,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.md),
            ) {
                CircularImage(
                    imageUrl = photoUrl,
                    fallbackText = username,
                    fallbackIcon = QodeNavigationIcons.Profile,
                    size = 80.dp,
                    contentDescription = null,
                )

                if (username != null) {
                    Text(
                        text = stringResource(R.string.block_user_message, username),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.block_user_message_no_username),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }

                Text(
                    text = stringResource(R.string.block_user_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            DialogButtonRow(
                dismissText = stringResource(CoreUiR.string.close),
                confirmText = stringResource(R.string.block_confirm),
                onDismiss = onCancel,
                onConfirm = onConfirm,
                isLoading = isLoading,
                isDestructive = true,
            )
        },
        dismissButton = {},
    )
}

@Composable
private fun ErrorDialogContent(
    error: OperationError,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.md),
            ) {
                Text(
                    text = stringResource(R.string.block_user_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = error.toUiText().asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            DialogButtonRow(
                dismissText = stringResource(CoreUiR.string.close),
                confirmText = stringResource(CoreUiR.string.action_retry),
                onDismiss = onCancel,
                onConfirm = onDismiss,
                isLoading = false,
                isDestructive = false,
            )
        },
        dismissButton = {},
    )
}

@Composable
private fun DialogButtonRow(
    dismissText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean,
    isDestructive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        QodeOutlinedButton(
            onClick = onDismiss,
            text = dismissText,
            enabled = !isLoading,
            modifier = Modifier.weight(1f),
        )

        QodeButton(
            onClick = onConfirm,
            text = confirmText,
            containerColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            loading = isLoading,
            modifier = Modifier.weight(1f),
        )
    }
}

@PreviewLightDark
@Composable
private fun ConfirmationDialogPreview() {
    QodeTheme {
        ConfirmationDialogContent(
            username = "johndoe",
            photoUrl = null,
            isLoading = false,
            onConfirm = {},
            onCancel = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ConfirmationDialogLoadingPreview() {
    QodeTheme {
        ConfirmationDialogContent(
            username = "johndoe",
            photoUrl = null,
            isLoading = true,
            onConfirm = {},
            onCancel = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ErrorDialogPreview() {
    QodeTheme {
        ErrorDialogContent(
            error = SystemError.Offline,
            onDismiss = {},
            onCancel = {},
        )
    }
}
