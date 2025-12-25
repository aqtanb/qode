package com.qodein.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.text.asString
import com.qodein.feature.auth.component.AuthenticationBottomSheet
import com.qodein.feature.auth.component.LegalDocumentBottomSheet
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.DocumentType
import com.qodein.shared.model.GoogleAuthResult
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
                authUser = state.authUser,
                isLoading = isLoading,
                error = error,
                onAccept = { viewModel.acceptConsent(state.authUser) },
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

    LaunchedEffect(Unit) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow(AuthBottomSheetViewModel.AUTH_RESULT_KEY, "")
            ?.collect { result ->
                if (result == AuthBottomSheetViewModel.AUTH_RESULT_SUCCESS) {
                    // Set the result on the previous screen's savedStateHandle
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(AuthBottomSheetViewModel.AUTH_RESULT_KEY, AuthBottomSheetViewModel.AUTH_RESULT_SUCCESS)
                    navController.popBackStack()
                }
            }
    }
}

@Composable
private fun ConsentDialog(
    authUser: GoogleAuthResult,
    isLoading: Boolean,
    error: OperationError?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onShowTerms: () -> Unit,
    onShowPrivacy: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isChecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { /* Cannot dismiss - user must choose */ },
        title = {
            Text(
                text = stringResource(CoreUiR.string.legal_consent_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                // Subtitle
                Text(
                    text = stringResource(CoreUiR.string.legal_consent_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Checkbox with legal links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top,
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it },
                        enabled = !isLoading,
                    )

                    val annotatedText = buildAnnotatedString {
                        append(stringResource(CoreUiR.string.legal_consent_checkbox_prefix))
                        append(" ")

                        pushStringAnnotation(tag = "terms", annotation = "terms")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                            ),
                        ) {
                            append(stringResource(CoreUiR.string.terms_of_service))
                        }
                        pop()

                        append(" ")
                        append(stringResource(CoreUiR.string.legal_consent_and))
                        append(" ")

                        pushStringAnnotation(tag = "privacy", annotation = "privacy")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                            ),
                        ) {
                            append(stringResource(CoreUiR.string.privacy_policy))
                        }
                        pop()
                    }

                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = SpacingTokens.xs),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(tag = "terms", start = offset, end = offset)
                                .firstOrNull()?.let { onShowTerms() }
                            annotatedText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                                .firstOrNull()?.let { onShowPrivacy() }
                        },
                    )
                }

                // Error message
                error?.let { err ->
                    Text(
                        text = err.toUiText().asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = SpacingTokens.xs),
                    )
                    LaunchedEffect(err) {
                        kotlinx.coroutines.delay(3000)
                        onDismissError()
                    }
                }
            }
        },
        confirmButton = {
            QodeButton(
                onClick = onAccept,
                text = if (isLoading) {
                    stringResource(CoreUiR.string.loading)
                } else {
                    stringResource(CoreUiR.string.legal_consent_accept)
                },
                enabled = isChecked && !isLoading,
                loading = isLoading,
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDecline,
                enabled = !isLoading,
            ) {
                Text(stringResource(CoreUiR.string.legal_consent_decline))
            }
        },
        modifier = modifier,
    )
}
