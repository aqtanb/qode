package com.qodein.qode.ui.error

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Support
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qodein.shared.common.result.ErrorAction
import com.qodein.shared.common.result.ErrorType

/**
 * Reusable error display components using proper ErrorType classification
 * and localized string resources.
 */

/**
 * Full-screen error content with retry actions.
 * Use this for main content areas when data loading fails.
 */
@Composable
fun ErrorContent(
    errorType: ErrorType,
    isRetryable: Boolean,
    errorCode: String? = null,
    onRetry: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onContactSupport: () -> Unit = {},
    onCheckNetwork: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val errorMessage = errorType.toLocalizedMessage()
    val suggestedAction = when (errorType) {
        ErrorType.NETWORK_TIMEOUT,
        ErrorType.NETWORK_NO_CONNECTION,
        ErrorType.NETWORK_HOST_UNREACHABLE,
        ErrorType.NETWORK_GENERAL -> ErrorAction.CHECK_NETWORK

        ErrorType.AUTH_INVALID_CREDENTIALS,
        ErrorType.AUTH_USER_NOT_FOUND -> ErrorAction.SIGN_IN

        ErrorType.AUTH_PERMISSION_DENIED,
        ErrorType.AUTH_UNAUTHORIZED -> ErrorAction.CONTACT_SUPPORT

        ErrorType.AUTH_USER_CANCELLED -> ErrorAction.DISMISS_ONLY

        ErrorType.VALIDATION_REQUIRED_FIELD,
        ErrorType.VALIDATION_INVALID_FORMAT,
        ErrorType.VALIDATION_TOO_SHORT,
        ErrorType.VALIDATION_TOO_LONG -> ErrorAction.DISMISS_ONLY

        else -> if (isRetryable) ErrorAction.RETRY else ErrorAction.CONTACT_SUPPORT
    }

    val errorIcon = when (errorType) {
        ErrorType.NETWORK_TIMEOUT,
        ErrorType.NETWORK_NO_CONNECTION,
        ErrorType.NETWORK_HOST_UNREACHABLE,
        ErrorType.NETWORK_GENERAL -> Icons.Outlined.NetworkCheck
        else -> Icons.Outlined.ErrorOutline
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                imageVector = errorIcon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics {
                    contentDescription = "Error: $errorMessage"
                },
            )

            // Show error code for debugging (only in debug builds ideally)
            errorCode?.let { code ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Error Code: $code",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons based on suggested action
            when (suggestedAction) {
                ErrorAction.RETRY -> {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.semantics {
                            contentDescription = "Retry loading"
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = ErrorAction.RETRY.toLocalizedActionText())
                    }
                }

                ErrorAction.SIGN_IN -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onNavigateToAuth,
                            modifier = Modifier.semantics {
                                contentDescription = "Navigate to sign in"
                            },
                        ) {
                            Text(text = ErrorAction.SIGN_IN.toLocalizedActionText())
                        }

                        if (isRetryable) {
                            TextButton(onClick = onRetry) {
                                Text(text = ErrorAction.RETRY.toLocalizedActionText())
                            }
                        }
                    }
                }

                ErrorAction.CHECK_NETWORK -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(
                            onClick = onCheckNetwork,
                            modifier = Modifier.semantics {
                                contentDescription = "Check network settings"
                            },
                        ) {
                            Text(text = ErrorAction.CHECK_NETWORK.toLocalizedActionText())
                        }

                        Button(onClick = onRetry) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = ErrorAction.RETRY.toLocalizedActionText())
                        }
                    }
                }

                ErrorAction.CONTACT_SUPPORT -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onContactSupport,
                            modifier = Modifier.semantics {
                                contentDescription = "Contact support"
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Support,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = ErrorAction.CONTACT_SUPPORT.toLocalizedActionText())
                        }

                        if (isRetryable) {
                            OutlinedButton(onClick = onRetry) {
                                Text(text = ErrorAction.RETRY.toLocalizedActionText())
                            }
                        }
                    }
                }

                ErrorAction.DISMISS_ONLY -> {
                    // No action buttons for dismiss-only errors
                    // These are typically validation errors shown inline
                }
            }
        }
    }
}

/**
 * Inline error message for form fields and validation.
 * Use this for showing validation errors next to form inputs.
 */
@Composable
fun InlineError(
    errorType: ErrorType,
    modifier: Modifier = Modifier
) {
    val errorMessage = errorType.toLocalizedMessage()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.error,
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics {
                contentDescription = "Validation error: $errorMessage"
            },
        )
    }
}

/**
 * Error snackbar for temporary error messages.
 * Use this with SnackbarHost to show system errors.
 */
@Composable
fun ErrorSnackbar(
    errorType: ErrorType,
    isRetryable: Boolean,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit = {},
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val errorMessage = errorType.toLocalizedMessage()
    val suggestedAction = when (errorType) {
        ErrorType.NETWORK_TIMEOUT,
        ErrorType.NETWORK_NO_CONNECTION,
        ErrorType.NETWORK_HOST_UNREACHABLE,
        ErrorType.NETWORK_GENERAL -> ErrorAction.CHECK_NETWORK

        ErrorType.AUTH_INVALID_CREDENTIALS,
        ErrorType.AUTH_USER_NOT_FOUND -> ErrorAction.SIGN_IN

        else -> if (isRetryable) ErrorAction.RETRY else ErrorAction.CONTACT_SUPPORT
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
    ) { snackbarData ->
        Snackbar(
            modifier = Modifier.semantics {
                contentDescription = "Error notification: $errorMessage"
            },
            action = {
                when (suggestedAction) {
                    ErrorAction.RETRY -> {
                        TextButton(onClick = onRetry) {
                            Text(text = ErrorAction.RETRY.toLocalizedActionText())
                        }
                    }

                    ErrorAction.SIGN_IN,
                    ErrorAction.CHECK_NETWORK,
                    ErrorAction.CONTACT_SUPPORT -> {
                        TextButton(onClick = onActionClick) {
                            Text(text = suggestedAction.toLocalizedActionText())
                        }
                    }

                    ErrorAction.DISMISS_ONLY -> {
                        TextButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                            Text(text = ErrorAction.DISMISS_ONLY.toLocalizedActionText())
                        }
                    }
                }
            },
        ) {
            Text(text = errorMessage)
        }
    }
}

/**
 * Compact error message for small spaces.
 * Use this in cards or compact layouts.
 */
@Composable
fun CompactError(
    errorType: ErrorType,
    isRetryable: Boolean,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val errorMessage = errorType.toLocalizedMessage()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (isRetryable) {
            TextButton(onClick = onRetry) {
                Text(
                    text = ErrorAction.RETRY.toLocalizedActionText(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
