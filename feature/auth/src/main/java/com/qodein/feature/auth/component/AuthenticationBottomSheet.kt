package com.qodein.feature.auth.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeTextButton
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.R
import com.qodein.core.ui.component.QodeEmailSignInButton
import com.qodein.core.ui.component.QodeGoogleSignInButton
import com.qodein.core.ui.component.SignInButtonHeight
import com.qodein.core.ui.component.SignInButtonShape
import com.qodein.core.ui.error.asUiText
import com.qodein.feature.auth.EmailSignInUiState
import com.qodein.shared.common.error.OperationError

/**
 * Modern authentication prompt using bottom sheet instead of dialog
 *
 * Provides contextual messaging based on the action that triggered the auth requirement.
 * Non-intrusive, easy to dismiss, with one-tap Google Sign-In.
 *
 * @param authPromptAction The action that triggered the authentication prompt
 * @param onSignInClick Called when user clicks the sign-in button
 * @param onDismiss Called when user dismisses the bottom sheet
 * @param modifier Modifier to be applied to the bottom sheet
 * @param sheetState State for controlling the bottom sheet behavior
 * @param isLoading Whether the sign-in process is currently loading
 * @param errorType Optional error to show in snackbar
 * @param onErrorDismissed Called when error snackbar is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationBottomSheet(
    authPromptAction: AuthPromptAction,
    onSignInClick: () -> Unit,
    onDismiss: () -> Unit,
    emailState: EmailSignInUiState,
    emailActions: EmailSignInActions,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    isLoading: Boolean = false,
    error: OperationError? = null,
    onErrorDismissed: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
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
                action = authPromptAction,
                onSignInClick = onSignInClick,
                onDismiss = onDismiss,
                emailState = emailState,
                emailActions = emailActions,
                isLoading = isLoading,
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
    emailState: EmailSignInUiState,
    emailActions: EmailSignInActions,
    isLoading: Boolean,
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
                    imageVector = ActionIcons.Close,
                    contentDescription = stringResource(R.string.ui_close),
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

        if (emailState.isExpanded) {
            EmailSignInForm(
                state = emailState,
                actions = emailActions,
                isLoading = isLoading,
            )
        } else {
            QodeGoogleSignInButton(
                onClick = onSignInClick,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            QodeEmailSignInButton(
                onClick = emailActions.onExpand,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.auth_bottom_sheet_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = SpacingTokens.xs),
            )
        }
    }
}

data class EmailSignInActions(
    val onExpand: () -> Unit,
    val onEmailChange: (String) -> Unit,
    val onSendCode: () -> Unit,
    val onCodeChange: (String) -> Unit,
    val onSignIn: () -> Unit,
    val onChangeEmail: () -> Unit,
    val onCollapse: () -> Unit
)

@Composable
private fun EmailSignInForm(
    state: EmailSignInUiState,
    actions: EmailSignInActions,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        if (!state.isCodeSent) {
            SignInTextField(
                value = state.email,
                onValueChange = actions.onEmailChange,
                placeholder = stringResource(R.string.auth_email_placeholder),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { actions.onSendCode() }),
            )
            SignInPrimaryButton(
                onClick = actions.onSendCode,
                text = stringResource(R.string.auth_send_code),
                enabled = state.email.isNotBlank(),
                loading = isLoading,
            )
            QodeTextButton(
                onClick = actions.onCollapse,
                text = stringResource(R.string.auth_other_options),
                enabled = !isLoading,
            )
        } else {
            Text(
                text = stringResource(R.string.auth_code_sent, state.email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            SignInTextField(
                value = state.code,
                onValueChange = actions.onCodeChange,
                placeholder = stringResource(R.string.auth_code_placeholder),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { actions.onSignIn() }),
                textStyle = MaterialTheme.typography.titleLarge.copy(letterSpacing = 8.sp, textAlign = TextAlign.Center),
            )
            SignInPrimaryButton(
                onClick = actions.onSignIn,
                text = stringResource(R.string.auth_sign_in),
                enabled = state.code.length == CODE_LENGTH,
                loading = isLoading,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                QodeTextButton(
                    onClick = actions.onChangeEmail,
                    text = stringResource(R.string.auth_change_email),
                    enabled = !isLoading,
                )
                QodeTextButton(
                    onClick = actions.onSendCode,
                    text = stringResource(R.string.auth_resend_code),
                    enabled = !isLoading,
                )
            }
        }
    }
}

private const val CODE_LENGTH = 6

@Composable
private fun SignInTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = textStyle.copy(letterSpacing = MaterialTheme.typography.bodyLarge.letterSpacing),
                modifier = Modifier.fillMaxWidth(),
                textAlign = textStyle.textAlign,
            )
        },
        enabled = enabled,
        singleLine = true,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = SignInButtonShape,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = SignInButtonHeight),
    )
}

@Composable
private fun SignInPrimaryButton(
    onClick: () -> Unit,
    text: String,
    enabled: Boolean,
    loading: Boolean
) {
    QodeButton(
        onClick = onClick,
        text = text,
        enabled = enabled && !loading,
        loading = loading,
        size = ButtonSize.Large,
        shape = SignInButtonShape,
        modifier = Modifier
            .fillMaxWidth()
            .height(SignInButtonHeight),
    )
}

private val PreviewEmailActions = EmailSignInActions({}, {}, {}, {}, {}, {}, {})

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun AuthenticationBottomSheetPostSubmissionPreview() {
    QodeTheme {
        Surface {
            AuthenticationBottomSheetContent(
                action = AuthPromptAction.CreatePost,
                onSignInClick = {},
                onDismiss = {},
                emailState = EmailSignInUiState(),
                emailActions = PreviewEmailActions,
                isLoading = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EmailSignInFormCodePreview() {
    QodeTheme {
        Surface {
            EmailSignInForm(
                state = EmailSignInUiState(isExpanded = true, email = "user@example.com", isCodeSent = true),
                actions = PreviewEmailActions,
                isLoading = false,
            )
        }
    }
}
