package com.qodein.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSize
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Dialog types for different use cases
 */
enum class QodeDialogType {
    Alert,
    Confirmation,
    FullScreen,
    BottomSheet
}

/**
 * Production-ready dialog component for Qode design system
 *
 * @param onDismissRequest Called when the user tries to dismiss the dialog
 * @param modifier Modifier to be applied to the dialog
 * @param type The type of dialog
 * @param title The title of the dialog
 * @param text The body text of the dialog
 * @param icon Optional icon to display
 * @param confirmButton The confirm button configuration
 * @param dismissButton Optional dismiss button configuration
 * @param shape The shape of the dialog
 * @param containerColor The background color of the dialog
 * @param properties Dialog properties
 * @param content Custom content for the dialog (overrides text)
 */
@Composable
fun QodeDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    type: QodeDialogType = QodeDialogType.Alert,
    title: String? = null,
    text: String? = null,
    icon: ImageVector? = null,
    confirmButton: DialogButton,
    dismissButton: DialogButton? = null,
    shape: Shape = RoundedCornerShape(QodeCorners.lg),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    properties: DialogProperties = DialogProperties(),
    content: (@Composable () -> Unit)? = null
) {
    when (type) {
        QodeDialogType.Alert -> {
            AlertDialog(
                onDismissRequest = onDismissRequest,
                modifier = modifier,
                title = title?.let {
                    {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                },
                text = content
                    ?: text?.let {
                        {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    },
                icon = icon?.let {
                    {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier.size(QodeSize.iconLarge),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                confirmButton = {
                    QodeButton(
                        onClick = confirmButton.onClick,
                        text = confirmButton.text,
                        variant = confirmButton.variant,
                        enabled = confirmButton.enabled,
                    )
                },
                dismissButton = dismissButton?.let {
                    {
                        QodeButton(
                            onClick = it.onClick,
                            text = it.text,
                            variant = it.variant,
                            enabled = it.enabled,
                        )
                    }
                },
                shape = shape,
                containerColor = containerColor,
                properties = properties,
            )
        }

        QodeDialogType.Confirmation -> {
            ConfirmationDialog(
                onDismissRequest = onDismissRequest,
                modifier = modifier,
                title = title,
                text = text,
                icon = icon,
                confirmButton = confirmButton,
                dismissButton = dismissButton,
                shape = shape,
                containerColor = containerColor,
                properties = properties,
                content = content,
            )
        }

        QodeDialogType.FullScreen -> {
            FullScreenDialog(
                onDismissRequest = onDismissRequest,
                title = title ?: "",
                confirmButton = confirmButton,
                dismissButton = dismissButton,
                content = content ?: {},
            )
        }

        QodeDialogType.BottomSheet -> {
            // This would typically use ModalBottomSheet, but for simplicity
            // we'll use a regular dialog positioned at the bottom
            BottomSheetDialog(
                onDismissRequest = onDismissRequest,
                title = title,
                confirmButton = confirmButton,
                dismissButton = dismissButton,
                content = {
                    if (content != null) {
                        content()
                    } else {
                        text?.let { Text(it) }
                    }
                },
            )
        }
    }
}

/**
 * Confirmation dialog with emphasis on the action
 */
@Composable
private fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    title: String?,
    text: String?,
    icon: ImageVector?,
    confirmButton: DialogButton,
    dismissButton: DialogButton?,
    shape: Shape,
    containerColor: Color,
    properties: DialogProperties,
    content: (@Composable () -> Unit)?
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                icon?.let {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(QodeCorners.md),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                modifier = Modifier.size(QodeSize.iconLarge),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(SpacingTokens.md))
                }

                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(SpacingTokens.sm))
                }

                if (content != null) {
                    content()
                } else {
                    text?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    dismissButton?.let {
                        QodeButton(
                            onClick = it.onClick,
                            text = it.text,
                            variant = it.variant,
                            modifier = Modifier.weight(1f),
                            enabled = it.enabled,
                        )
                    }
                    QodeButton(
                        onClick = confirmButton.onClick,
                        text = confirmButton.text,
                        variant = confirmButton.variant,
                        modifier = if (dismissButton != null) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                        enabled = confirmButton.enabled,
                    )
                }
            }
        }
    }
}

/**
 * Full screen dialog for complex content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButton: DialogButton,
    dismissButton: DialogButton?,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                            )
                        }
                    },
                    actions = {
                        QodeButton(
                            onClick = confirmButton.onClick,
                            text = confirmButton.text,
                            variant = QodeButtonVariant.Text,
                            enabled = confirmButton.enabled,
                        )
                    },
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(SpacingTokens.md),
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * Bottom sheet style dialog
 */
@Composable
private fun BottomSheetDialog(
    onDismissRequest: () -> Unit,
    title: String?,
    confirmButton: DialogButton,
    dismissButton: DialogButton?,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(QodeAnimation.MEDIUM),
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(QodeAnimation.MEDIUM),
                ),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(
                        topStart = QodeCorners.xl,
                        topEnd = QodeCorners.xl,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp,
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(SpacingTokens.lg),
                    ) {
                        // Handle bar
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(48.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(ShapeTokens.Corner.full),
                                ),
                        )

                        Spacer(modifier = Modifier.height(SpacingTokens.md))

                        title?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = SpacingTokens.md),
                            )
                        }

                        content()

                        Spacer(modifier = Modifier.height(SpacingTokens.lg))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                        ) {
                            dismissButton?.let {
                                QodeButton(
                                    onClick = it.onClick,
                                    text = it.text,
                                    variant = it.variant,
                                    modifier = Modifier.weight(1f),
                                    enabled = it.enabled,
                                )
                            }
                            QodeButton(
                                onClick = confirmButton.onClick,
                                text = confirmButton.text,
                                variant = confirmButton.variant,
                                modifier = if (dismissButton != null) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                                enabled = confirmButton.enabled,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data class for dialog button configuration
 */
data class DialogButton(
    val text: String,
    val onClick: () -> Unit,
    val variant: QodeButtonVariant = QodeButtonVariant.Primary,
    val enabled: Boolean = true
)

/**
 * Simple confirmation dialog
 */
@Composable
fun QodeSimpleDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel"
) {
    QodeDialog(
        onDismissRequest = onDismiss,
        type = QodeDialogType.Alert,
        title = title,
        text = message,
        confirmButton = DialogButton(
            text = confirmText,
            onClick = onConfirm,
        ),
        dismissButton = DialogButton(
            text = dismissText,
            onClick = onDismiss,
            variant = QodeButtonVariant.Text,
        ),
    )
}

// Previews
@Preview(name = "Dialog Types", showBackground = true)
@Composable
private fun QodeDialogPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            var showAlert by remember { mutableStateOf(false) }
            var showConfirmation by remember { mutableStateOf(false) }
            var showFullScreen by remember { mutableStateOf(false) }
            var showBottomSheet by remember { mutableStateOf(false) }

            QodeButton(
                onClick = { showAlert = true },
                text = "Show Alert Dialog",
            )

            QodeButton(
                onClick = { showConfirmation = true },
                text = "Show Confirmation Dialog",
            )

            QodeButton(
                onClick = { showFullScreen = true },
                text = "Show Full Screen Dialog",
            )

            QodeButton(
                onClick = { showBottomSheet = true },
                text = "Show Bottom Sheet Dialog",
            )

            if (showAlert) {
                QodeDialog(
                    onDismissRequest = { showAlert = false },
                    type = QodeDialogType.Alert,
                    title = "Delete Promo Code?",
                    text = "This action cannot be undone. The promo code will be permanently removed.",
                    icon = Icons.Default.Delete,
                    confirmButton = DialogButton(
                        text = "Delete",
                        onClick = { showAlert = false },
                    ),
                    dismissButton = DialogButton(
                        text = "Cancel",
                        onClick = { showAlert = false },
                        variant = QodeButtonVariant.Text,
                    ),
                )
            }

            if (showConfirmation) {
                QodeDialog(
                    onDismissRequest = { showConfirmation = false },
                    type = QodeDialogType.Confirmation,
                    title = "Enable Notifications?",
                    text = "Get instant alerts when new promo codes are added for your favorite stores.",
                    icon = Icons.Default.Notifications,
                    confirmButton = DialogButton(
                        text = "Enable",
                        onClick = { showConfirmation = false },
                    ),
                    dismissButton = DialogButton(
                        text = "Not Now",
                        onClick = { showConfirmation = false },
                        variant = QodeButtonVariant.Outlined,
                    ),
                )
            }

            if (showFullScreen) {
                QodeDialog(
                    onDismissRequest = { showFullScreen = false },
                    type = QodeDialogType.FullScreen,
                    title = "Add Promo Code",
                    confirmButton = DialogButton(
                        text = "Save",
                        onClick = { showFullScreen = false },
                    ),
                    content = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                        ) {
                            QodeTextField(
                                value = "",
                                onValueChange = {},
                                label = "Store Name",
                                required = true,
                            )
                            QodeTextField(
                                value = "",
                                onValueChange = {},
                                label = "Promo Code",
                                required = true,
                            )
                            QodeTextField(
                                value = "",
                                onValueChange = {},
                                label = "Description",
                                variant = QodeTextFieldVariant.Multiline,
                            )
                        }
                    },
                )
            }

            if (showBottomSheet) {
                QodeDialog(
                    onDismissRequest = { showBottomSheet = false },
                    type = QodeDialogType.BottomSheet,
                    title = "Share Promo Code",
                    confirmButton = DialogButton(
                        text = "Share",
                        onClick = { showBottomSheet = false },
                    ),
                    dismissButton = DialogButton(
                        text = "Cancel",
                        onClick = { showBottomSheet = false },
                        variant = QodeButtonVariant.Text,
                    ),
                    content = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(SpacingTokens.sm),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(QodeCorners.md),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(56.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Share,
                                                contentDescription = "WhatsApp",
                                            )
                                        }
                                    }
                                    Text(
                                        "WhatsApp",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(top = SpacingTokens.xs),
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(SpacingTokens.sm),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(QodeCorners.md),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.size(56.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Telegram",
                                            )
                                        }
                                    }
                                    Text(
                                        "Telegram",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(top = SpacingTokens.xs),
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(SpacingTokens.sm),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(QodeCorners.md),
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        modifier = Modifier.size(56.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Link,
                                                contentDescription = "Copy Link",
                                            )
                                        }
                                    }
                                    Text(
                                        "Copy Link",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(top = SpacingTokens.xs),
                                    )
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}
