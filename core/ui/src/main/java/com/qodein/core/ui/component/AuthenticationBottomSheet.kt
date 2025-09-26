package com.qodein.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.core.ui.error.asUiText
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError

/**
 * Authentication prompt actions that trigger contextual auth prompts
 */
enum class AuthPromptAction(val titleResId: Int, val messageResId: Int, val iconVector: ImageVector) {
    SubmitPromoCode(
        titleResId = R.string.auth_submit_promo_title,
        messageResId = R.string.auth_submit_promo_message,
        iconVector = QodeActionIcons.Add,
    ),
    UpvotePromoCode(
        titleResId = R.string.auth_upvote_title,
        messageResId = R.string.auth_upvote_message,
        iconVector = QodeActionIcons.Thumbs,
    ),
    DownvotePromoCode(
        titleResId = R.string.auth_downvote_title,
        messageResId = R.string.auth_downvote_message,
        iconVector = QodeActionIcons.ThumbsDown,
    ),
    WriteComment(
        titleResId = R.string.auth_comment_title,
        messageResId = R.string.auth_comment_message,
        iconVector = QodeActionIcons.Comment,
    ),
    BookmarkPromoCode(
        titleResId = R.string.auth_bookmark_promo_title,
        messageResId = R.string.auth_bookmark_promo_message,
        iconVector = QodeActionIcons.Bookmark,
    ),
    FollowStore(
        titleResId = R.string.auth_follow_store_title,
        messageResId = R.string.auth_follow_store_message,
        iconVector = QodeActionIcons.Follow,
    )
}

/**
 * Modern authentication prompt using bottom sheet instead of dialog
 *
 * Provides contextual messaging based on the action that triggered the auth requirement.
 * Non-intrusive, easy to dismiss, with one-tap Google Sign-In.
 *
 * @param action The action that triggered the authentication prompt
 * @param onSignInClick Called when user clicks the sign-in button
 * @param onDismiss Called when user dismisses the bottom sheet
 * @param modifier Modifier to be applied to the bottom sheet
 * @param sheetState State for controlling the bottom sheet behavior
 * @param isLoading Whether the sign-in process is currently loading
 * @param errorType Optional error to show in snackbar
 * @param onErrorDismissed Called when error snackbar is dismissed
 * @param isDarkTheme Whether to use dark theme styling (from app preferences)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationBottomSheet(
    action: AuthPromptAction,
    onSignInClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    isLoading: Boolean = false,
    error: OperationError? = null,
    onErrorDismissed: () -> Unit = {},
    isDarkTheme: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = ShapeTokens.Corner.extraLarge,
            topEnd = ShapeTokens.Corner.extraLarge,
        ),
        dragHandle = {
            // Custom drag handle with better visual hierarchy
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = SpacingTokens.sm),
            ) {
                Canvas(
                    modifier = Modifier.size(width = SpacingTokens.xl, height = SpacingTokens.xxxs),
                ) {
                    drawRoundRect(
                        color = Color.Gray.copy(alpha = 0.4f),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                    )
                }
            }
        },
    ) {
        val snackbarHostState = remember { SnackbarHostState() }

        // Get localized error message in composable context
        val errorMessage = error?.asUiText()

        // Show error snackbar when there's an error
        LaunchedEffect(error) {
            errorMessage?.let { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                )
                onErrorDismissed()
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AuthenticationBottomSheetContent(
                action = action,
                onSignInClick = onSignInClick,
                onDismiss = onDismiss,
                isLoading = isLoading,
                isDarkTheme = isDarkTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.lg)
                    .padding(bottom = SpacingTokens.xl),
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(SpacingTokens.md),
            )
        }
    }
}

@Composable
private fun AuthenticationBottomSheetContent(
    action: AuthPromptAction,
    onSignInClick: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Header with close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onDismiss,
                enabled = !isLoading, // Disable dismiss during loading
            ) {
                Icon(
                    imageVector = QodeActionIcons.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Action icon with subtle container
        Box(
            modifier = Modifier
                .size(SpacingTokens.huge)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = action.iconVector,
                contentDescription = null,
                modifier = Modifier.size(SpacingTokens.xl),
                tint = MaterialTheme.colorScheme.onSurfaceVariant, // Decorative, not clickable
            )
        }

        // Contextual title
        Text(
            text = stringResource(action.titleResId),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        // Contextual message - lighter hierarchy
        Text(
            text = stringResource(action.messageResId),
            style = MaterialTheme.typography.bodyMedium, // Softer contrast
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
        )

        // Google Sign-In button
        QodeGoogleSignInButton(
            onClick = onSignInClick,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth(),
            isDarkTheme = isDarkTheme,
        )

        // Footer text - tighter to button
        Text(
            text = stringResource(R.string.auth_bottom_sheet_footer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), // Even more subtle
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = SpacingTokens.xs),
        )
    }
}

// MARK: - Previews

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Submit Promo Code Auth", showBackground = true)
@Composable
private fun AuthenticationBottomSheetSubmitPreview() {
    QodeTheme {
        AuthenticationBottomSheetContent(
            action = AuthPromptAction.SubmitPromoCode,
            onSignInClick = {},
            onDismiss = {},
            isLoading = false,
            isDarkTheme = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Upvote Auth", showBackground = true)
@Composable
private fun AuthenticationBottomSheetUpvotePreview() {
    QodeTheme {
        AuthenticationBottomSheetContent(
            action = AuthPromptAction.UpvotePromoCode,
            onSignInClick = {},
            onDismiss = {},
            isLoading = false,
            isDarkTheme = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Comment Auth", showBackground = true)
@Composable
private fun AuthenticationBottomSheetCommentPreview() {
    QodeTheme {
        AuthenticationBottomSheetContent(
            action = AuthPromptAction.WriteComment,
            onSignInClick = {},
            onDismiss = {},
            isLoading = false,
            isDarkTheme = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Bookmark Promo Code Auth", showBackground = true)
@Composable
private fun AuthenticationBottomSheetBookmarkPreview() {
    QodeTheme {
        AuthenticationBottomSheetContent(
            action = AuthPromptAction.BookmarkPromoCode,
            onSignInClick = {},
            onDismiss = {},
            isLoading = false,
            isDarkTheme = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Loading State", showBackground = true)
@Composable
private fun AuthenticationBottomSheetLoadingPreview() {
    QodeTheme {
        AuthenticationBottomSheetContent(
            action = AuthPromptAction.FollowStore,
            onSignInClick = {},
            onDismiss = {},
            isLoading = true,
            isDarkTheme = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Error State", showBackground = true)
@Composable
private fun AuthenticationBottomSheetErrorPreview() {
    QodeTheme {
        AuthenticationBottomSheet(
            action = AuthPromptAction.UpvotePromoCode,
            onSignInClick = {},
            onDismiss = {},
            isLoading = false,
            error = UserError.AuthenticationFailure.Cancelled,
            onErrorDismissed = {},
            isDarkTheme = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
