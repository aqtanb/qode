package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.core.ui.error.toLocalizedActionText
import com.qodein.shared.common.result.ErrorAction

/**
 * Enterprise-level error card component for consistent error handling across the app
 *
 * @param message The error message to display
 * @param modifier Modifier to be applied to the card
 * @param title The error title (defaults to localized "Something went wrong")
 * @param isRetryable Whether this error can be retried
 * @param onRetry Callback for retry action (only shown if isRetryable is true)
 * @param onDismiss Callback for dismiss action (always shown if provided)
 * @param icon Icon to display (defaults to warning icon)
 */
@Composable
fun QodeErrorCard(
    message: String,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.error_default_title),
    isRetryable: Boolean = true,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    icon: ImageVector = Icons.Default.Warning
) {
    QodeCard(
        variant = QodeCardVariant.Elevated,
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Dismiss button in top-right corner
            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(SpacingTokens.sm)
                        .size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_dismiss),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(SpacingTokens.xl)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                // Error title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )

                // Error message
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                // Retry button (only show if retryable and callback provided)
                if (isRetryable && onRetry != null) {
                    QodeButton(
                        text = stringResource(R.string.error_retry_button),
                        onClick = onRetry,
                        variant = QodeButtonVariant.Primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = SpacingTokens.sm),
                    )
                }
            }
        }
    }
}

// MARK: - Action-Based Error Card

/**
 * Error card with action-based button system for enterprise UX
 */
@Composable
fun QodeActionErrorCard(
    message: String,
    errorAction: ErrorAction,
    onActionClicked: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.error_default_title),
    onDismiss: (() -> Unit)? = null,
    icon: ImageVector = Icons.Default.Warning
) {
    QodeCard(
        variant = QodeCardVariant.Elevated,
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Dismiss button in top-right corner
            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(SpacingTokens.sm)
                        .size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_dismiss),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(SpacingTokens.xl)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                // Error title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )

                // Error message
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                // Action button (always shown unless DISMISS_ONLY)
                if (errorAction != ErrorAction.DISMISS_ONLY) {
                    QodeButton(
                        text = errorAction.toLocalizedActionText(),
                        onClick = onActionClicked,
                        variant = when (errorAction) {
                            ErrorAction.RETRY -> QodeButtonVariant.Primary
                            ErrorAction.SIGN_IN -> QodeButtonVariant.Primary
                            ErrorAction.CHECK_NETWORK -> QodeButtonVariant.Secondary
                            ErrorAction.CONTACT_SUPPORT -> QodeButtonVariant.Secondary
                            ErrorAction.DISMISS_ONLY -> QodeButtonVariant.Secondary // Won't be used
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = SpacingTokens.sm),
                    )
                }
            }
        }
    }
}

// MARK: - Convenience Functions

/**
 * Error card with retry functionality - most common use case
 */
@Composable
fun QodeRetryableErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.error_default_title),
    onDismiss: (() -> Unit)? = null
) {
    QodeErrorCard(
        message = message,
        onRetry = onRetry,
        onDismiss = onDismiss,
        isRetryable = true,
        title = title,
        modifier = modifier,
    )
}

/**
 * Error card without retry - for non-recoverable errors
 */
@Composable
fun QodeNonRetryableErrorCard(
    message: String,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.error_default_title),
    onDismiss: (() -> Unit)? = null
) {
    QodeErrorCard(
        message = message,
        isRetryable = false,
        onRetry = null,
        onDismiss = onDismiss,
        title = title,
        modifier = modifier,
    )
}

// MARK: - Enterprise Previews

@Preview(
    name = "Error Card - Retryable",
    showBackground = true,
    group = "Error Cards",
)
@Composable
private fun QodeRetryableErrorCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            QodeRetryableErrorCard(
                message = "Failed to load profile data. Please check your connection and try again.",
                onRetry = {},
            )
        }
    }
}

@Preview(
    name = "Error Card - Non-retryable",
    showBackground = true,
    group = "Error Cards",
)
@Composable
private fun QodeNonRetryableErrorCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            QodeNonRetryableErrorCard(
                message = "This feature is not available in your region.",
                title = "Feature Unavailable",
            )
        }
    }
}

@Preview(
    name = "Error Cards - Various States",
    showBackground = true,
    heightDp = 800,
    group = "Error Cards",
)
@Composable
private fun QodeErrorCardsPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            // Network error - retryable
            QodeErrorCard(
                message = "Failed to connect to server. Please check your internet connection.",
                onRetry = {},
                title = "Connection Error",
            )

            // Data not found - non-retryable
            QodeErrorCard(
                message = "The requested content could not be found.",
                isRetryable = false,
                title = "Content Not Found",
            )

            // Permission error - non-retryable
            QodeErrorCard(
                message = "You don't have permission to access this resource.",
                isRetryable = false,
                title = "Access Denied",
            )

            // Generic error with retry
            QodeErrorCard(
                message = "An unexpected error occurred while processing your request.",
            )
        }
    }
}

@Preview(
    name = "Error Cards - Dark Theme",
    showBackground = true,
    group = "Error Cards Themes",
)
@Composable
private fun QodeErrorCardsDarkPreview() {
    QodeTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            QodeRetryableErrorCard(
                message = "Failed to load data. Please try again.",
                onRetry = {},
            )

            QodeNonRetryableErrorCard(
                message = "This action is not supported.",
                title = "Unsupported Action",
            )
        }
    }
}
