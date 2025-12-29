package com.qodein.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.text.asString
import com.qodein.feature.auth.component.AuthenticationBottomSheet
import com.qodein.feature.auth.component.LegalDocumentBottomSheet
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.DocumentType
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import com.qodein.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthPromptScreen(
    authPromptAction: AuthPromptAction,
    navController: NavController,
    viewModel: AuthBottomSheetViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val legalDocumentState by viewModel.legalDocumentState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (val state = uiState) {
        AuthBottomSheetUiState.SignIn -> {
            AuthenticationBottomSheet(
                authPromptAction = authPromptAction,
                onSignInClick = { viewModel.signInWithGoogle(context) },
                onDismiss = { navController.popBackStack() },
                isLoading = isLoading,
                error = error,
                onErrorDismissed = viewModel::dismissError,
            )
        }
        is AuthBottomSheetUiState.ConsentRequired -> {
            ConsentDialog(
                isLoading = isLoading,
                error = error,
                onAccept = {
                    viewModel.acceptConsent(state.authUser)
                },
                onDecline = {
                    viewModel.declineConsent()
                    navController.popBackStack()
                },
                onShowTerms = { viewModel.getLegalDocument(DocumentType.TermsOfService) },
                onShowPrivacy = { viewModel.getLegalDocument(DocumentType.PrivacyPolicy) },
                onDismissError = viewModel::dismissError,
            )
        }
    }

    LegalDocumentBottomSheet(
        onDismiss = viewModel::dismissLegalDocument,
        onRetry = viewModel::getLegalDocument,
        state = legalDocumentState,
    )

    val authResult by viewModel.savedStateHandle
        .getStateFlow(AuthBottomSheetViewModel.AUTH_RESULT_KEY, "")
        .collectAsStateWithLifecycle()

    LaunchedEffect(authResult) {
        if (authResult == AuthBottomSheetViewModel.AUTH_RESULT_SUCCESS) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(AuthBottomSheetViewModel.AUTH_RESULT_KEY, AuthBottomSheetViewModel.AUTH_RESULT_SUCCESS)
            viewModel.savedStateHandle[AuthBottomSheetViewModel.AUTH_RESULT_KEY] = ""
            navController.popBackStack()
        }
    }
}

@Composable
private fun ConsentDialog(
    isLoading: Boolean,
    error: OperationError?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onShowTerms: () -> Unit,
    onShowPrivacy: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var termsAccepted by remember { mutableStateOf(false) }
    var privacyAccepted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {},
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(CoreUiR.string.legal_consent_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                Text(
                    text = stringResource(CoreUiR.string.legal_consent_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        enabled = !isLoading,
                    )
                    Text(
                        text = stringResource(CoreUiR.string.legal_consent_checkbox_prefix),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = SpacingTokens.xs),
                    )
                    TextButton(onClick = onShowTerms) {
                        Text(
                            text = stringResource(CoreUiR.string.terms_of_service),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = privacyAccepted,
                        onCheckedChange = { privacyAccepted = it },
                        enabled = !isLoading,
                    )
                    Text(
                        text = stringResource(CoreUiR.string.legal_consent_checkbox_prefix),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = SpacingTokens.xs),
                    )
                    TextButton(onClick = onShowPrivacy) {
                        Text(
                            text = stringResource(CoreUiR.string.privacy_policy),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                error?.let { err ->
                    Text(
                        text = err.toUiText().asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = SpacingTokens.xs),
                    )
                    LaunchedEffect(err) {
                        delay(3000)
                        onDismissError()
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onDecline,
                        enabled = !isLoading,
                    ) {
                        Text(
                            text = stringResource(CoreUiR.string.legal_consent_decline),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    QodeButton(
                        onClick = onAccept,
                        text = if (isLoading) {
                            stringResource(CoreUiR.string.loading)
                        } else {
                            stringResource(CoreUiR.string.legal_consent_accept)
                        },
                        enabled = termsAccepted && privacyAccepted && !isLoading,
                        loading = isLoading,
                        modifier = Modifier.padding(start = SpacingTokens.sm),
                    )
                }
            }
        },
        confirmButton = {},
        modifier = modifier,
    )
}

@ThemePreviews
@Composable
private fun ConsentDialogPreview() {
    QodeTheme {
        ConsentDialog(
            isLoading = false,
            error = null,
            onAccept = {},
            onDecline = {},
            onShowTerms = {},
            onShowPrivacy = {},
            onDismissError = {},
        )
    }
}
