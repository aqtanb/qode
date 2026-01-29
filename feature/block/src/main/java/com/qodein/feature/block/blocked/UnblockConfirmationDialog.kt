package com.qodein.feature.block.blocked

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeOutlinedButton
import com.qodein.core.designsystem.component.QodeinAsyncImage
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens.Avatar
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.text.asString
import com.qodein.feature.block.R
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.core.ui.R as CoreUiR

@Composable
internal fun UnblockConfirmationDialog(
    username: String,
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
                QodeinAsyncImage(
                    imageUrl = photoUrl ?: "",
                    fallbackText = username,
                    size = Avatar.sizeLarge,
                    contentDescription = stringResource(CoreUiR.string.profile_picture_description),
                )

                Text(
                    text = stringResource(R.string.unblock_button, username),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = stringResource(R.string.unblock_user_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            DialogButtonRow(
                dismissText = stringResource(CoreUiR.string.ui_close),
                confirmText = stringResource(R.string.unblock_button),
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
                dismissText = stringResource(CoreUiR.string.ui_close),
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
private fun UnblockConfirmationDialogPreview() {
    QodeTheme {
        UnblockConfirmationDialog(
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
private fun UnblockConfirmationDialogLoadingPreview() {
    QodeTheme {
        UnblockConfirmationDialog(
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
