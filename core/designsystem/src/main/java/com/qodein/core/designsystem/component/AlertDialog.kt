package com.qodein.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Qode custom alert dialog with centralized buttons and consistent styling.
 *
 * Features:
 * - Centralized title and text
 * - Support for icon, title, text, or custom content
 * - Two-button layout with equal width
 * - Destructive action support (red confirm button)
 * - Loading state support
 * - Confirm button can be disabled
 *
 * @param onDismissRequest Called when the user tries to dismiss the dialog (e.g., by tapping outside)
 * @param confirmButtonText Text for the confirm button
 * @param onConfirmClick Called when the confirm button is clicked
 * @param modifier Modifier for the dialog
 * @param title Optional dialog title
 * @param text Optional dialog text/message
 * @param icon Optional icon to display at the top
 * @param dismissButtonText Optional text for the dismiss button (if null, only confirm button is shown)
 * @param onDismissClick Optional callback for dismiss button (defaults to onDismissRequest)
 * @param isLoading Whether the dialog is in loading state (disables buttons, shows loading spinner)
 * @param isDestructive Whether the confirm action is destructive (shows red button)
 * @param confirmEnabled Whether the confirm button is enabled
 * @param content Optional custom content composable for complex layouts
 */
@Composable
fun QodeAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String? = null,
    icon: ImageVector? = null,
    dismissButtonText: String? = null,
    onDismissClick: (() -> Unit)? = null,
    isLoading: Boolean = false,
    isDestructive: Boolean = false,
    confirmEnabled: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.md),
            ) {
                // Icon
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = when {
                            isDestructive -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(80.dp),
                    )
                }

                // Title
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Text
                if (text != null) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Custom content
                if (content != null) {
                    content()
                }
            }
        },
        confirmButton = {
            DialogButtonRow(
                dismissText = dismissButtonText,
                confirmText = confirmButtonText,
                onDismiss = onDismissClick ?: onDismissRequest,
                onConfirm = onConfirmClick,
                isLoading = isLoading,
                isDestructive = isDestructive,
                confirmEnabled = confirmEnabled,
            )
        },
        dismissButton = {},
        modifier = modifier,
    )
}

/**
 * Internal button row component for dialogs with consistent styling.
 */
@Composable
private fun DialogButtonRow(
    confirmText: String,
    onConfirm: () -> Unit,
    isLoading: Boolean,
    isDestructive: Boolean,
    confirmEnabled: Boolean,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        // Dismiss button (if provided)
        if (dismissText != null && onDismiss != null) {
            QodeOutlinedButton(
                onClick = onDismiss,
                text = dismissText,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
            )
        }

        // Confirm button
        QodeButton(
            onClick = onConfirm,
            text = confirmText,
            containerColor = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            loading = isLoading,
            enabled = confirmEnabled && !isLoading,
            modifier = Modifier.weight(1f),
        )
    }
}

// MARK: Previews

@PreviewLightDark
@Composable
private fun QodeAlertDialogBasicPreview() {
    QodeTheme {
        QodeAlertDialog(
            onDismissRequest = {},
            title = "Confirm Action",
            text = "Are you sure you want to proceed with this action?",
            confirmButtonText = "Confirm",
            onConfirmClick = {},
            dismissButtonText = "Cancel",
        )
    }
}

@PreviewLightDark
@Composable
private fun QodeAlertDialogWithIconPreview() {
    QodeTheme {
        QodeAlertDialog(
            onDismissRequest = {},
            icon = Icons.Default.Warning,
            title = "Warning",
            text = "This action cannot be undone. Please confirm that you want to proceed.",
            confirmButtonText = "Proceed",
            onConfirmClick = {},
            dismissButtonText = "Cancel",
        )
    }
}

@PreviewLightDark
@Composable
private fun QodeAlertDialogDestructivePreview() {
    QodeTheme {
        QodeAlertDialog(
            onDismissRequest = {},
            icon = Icons.Default.Error,
            title = "Delete Item",
            text = "This will permanently delete the item. This action cannot be undone.",
            confirmButtonText = "Delete",
            onConfirmClick = {},
            dismissButtonText = "Cancel",
            isDestructive = true,
        )
    }
}

@PreviewLightDark
@Composable
private fun QodeAlertDialogLoadingPreview() {
    QodeTheme {
        QodeAlertDialog(
            onDismissRequest = {},
            icon = Icons.Default.CheckCircle,
            title = "Processing",
            text = "Please wait while we process your request...",
            confirmButtonText = "Confirm",
            onConfirmClick = {},
            dismissButtonText = "Cancel",
            isLoading = true,
        )
    }
}

@PreviewLightDark
@Composable
private fun QodeAlertDialogSingleButtonPreview() {
    QodeTheme {
        QodeAlertDialog(
            onDismissRequest = {},
            icon = Icons.Default.Info,
            title = "Information",
            text = "Your changes have been saved successfully.",
            confirmButtonText = "OK",
            onConfirmClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun QodeAlertDialogWithCustomContentPreview() {
    QodeTheme {
        QodeAlertDialog(
            onDismissRequest = {},
            title = "Custom Content",
            confirmButtonText = "Accept",
            onConfirmClick = {},
            dismissButtonText = "Decline",
            content = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "This is custom content",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(SpacingTokens.sm))
                    Text(
                        text = "You can put anything here!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
    }
}
