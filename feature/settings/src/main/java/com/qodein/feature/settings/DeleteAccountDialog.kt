package com.qodein.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeAlertDialog
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.text.asString
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.core.ui.R as CoreUiR

/**
 * Dialog for confirming account deletion with checkbox confirmation.
 *
 * Features:
 * - Warning icon
 * - Clear warning message about permanent deletion
 * - Checkbox confirmation to enable delete button
 * - Loading state support
 * - Red destructive button
 *
 * @param isLoading Whether the deletion is in progress
 * @param onConfirm Called when the user confirms deletion
 * @param onDismiss Called when the user dismisses the dialog
 * @param modifier Modifier for the dialog
 */
@Composable
fun DeleteAccountDialog(
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var checkboxState by remember { mutableStateOf(false) }

    QodeAlertDialog(
        onDismissRequest = onDismiss,
        icon = QodeNavigationIcons.Error,
        title = stringResource(R.string.delete_account_dialog_title),
        confirmButtonText = stringResource(R.string.delete_account_confirm),
        onConfirmClick = onConfirm,
        dismissButtonText = stringResource(CoreUiR.string.cancel),
        onDismissClick = onDismiss,
        isLoading = isLoading,
        isDestructive = true,
        confirmEnabled = checkboxState && !isLoading,
        modifier = modifier,
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Warning message
                Text(
                    text = stringResource(R.string.delete_account_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                // Confirmation checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = checkboxState,
                        onCheckedChange = { checkboxState = it },
                        enabled = !isLoading,
                    )

                    Text(
                        text = stringResource(R.string.delete_account_checkbox),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
    )
}

/**
 * Error dialog shown when account deletion fails.
 *
 * @param error The error that occurred
 * @param onRetry Called when the user wants to retry
 * @param onDismiss Called when the user dismisses the error dialog
 * @param modifier Modifier for the dialog
 */
@Composable
fun DeleteAccountErrorDialog(
    error: OperationError,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeAlertDialog(
        onDismissRequest = onDismiss,
        icon = QodeNavigationIcons.Error,
        title = stringResource(R.string.delete_account_error_title),
        text = error.toUiText().asString(),
        confirmButtonText = stringResource(CoreUiR.string.action_retry),
        onConfirmClick = onRetry,
        dismissButtonText = stringResource(CoreUiR.string.close),
        onDismissClick = onDismiss,
        isDestructive = false,
        modifier = modifier,
    )
}

// MARK: Previews

@PreviewLightDark
@Composable
private fun DeleteAccountDialogPreview() {
    QodeTheme {
        DeleteAccountDialog(
            isLoading = false,
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun DeleteAccountDialogLoadingPreview() {
    QodeTheme {
        DeleteAccountDialog(
            isLoading = true,
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun DeleteAccountErrorDialogPreview() {
    QodeTheme {
        DeleteAccountErrorDialog(
            error = SystemError.Offline,
            onRetry = {},
            onDismiss = {},
        )
    }
}
