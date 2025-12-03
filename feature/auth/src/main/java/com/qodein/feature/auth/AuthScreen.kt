package com.qodein.feature.auth

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.QodeLogo
import com.qodein.core.designsystem.component.QodeLogoSize
import com.qodein.core.designsystem.component.QodeLogoStyle
import com.qodein.core.designsystem.component.QodeTextButton
import com.qodein.core.designsystem.component.QodeinElevatedCard
import com.qodein.core.designsystem.component.QodeinIconButton
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.QodeGoogleSignInButton
import com.qodein.core.ui.error.asUiText
import com.qodein.feature.auth.component.AuthTopAppBar
import com.qodein.feature.auth.component.LegalDocumentBottomSheet
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.DocumentType
import org.koin.androidx.compose.koinViewModel
import com.qodein.core.ui.R as CoreUiR

@Composable
fun AuthRoute(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel()
) {
    TrackScreenViewEvent(screenName = "Auth")

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val legalDocumentState by viewModel.legalDocumentState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.SignedIn -> onNavigateToHome()
            }
        }
    }

    AuthScreen(
        context = context,
        onAction = viewModel::handleAction,
        onBackClick = onNavigateToHome,
        authState = authState,
        legalDocumentState = legalDocumentState,
        modifier = modifier,
    )
}

@Composable
private fun AuthScreen(
    context: Context,
    onBackClick: () -> Unit,
    onAction: (AuthAction) -> Unit,
    authState: AuthUiState,
    legalDocumentState: LegalDocumentUiState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AuthTopAppBar(
                scrollState = scrollState,
                onBackClick = onBackClick,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = SpacingTokens.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AuthSignInCard(
                context = context,
                onAction = onAction,
                isLoading = authState.isSigningIn,
                modifier = Modifier.fillMaxWidth(),
            )

            if (authState.error != null) {
                AuthErrorMessage(
                    error = authState.error,
                    onDismiss = { onAction(AuthAction.AuthErrorDismissed) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SpacingTokens.md),
                )
            }
        }

        LegalDocumentBottomSheet(
            onDismiss = { onAction(AuthAction.LegalDocumentDismissed) },
            onRetry = { documentType -> onAction(AuthAction.LegalDocumentRetryClicked(documentType)) },
            state = legalDocumentState,
        )
    }
}

// MARK: - Components
@Composable
private fun AuthSignInCard(
    context: Context,
    onAction: (AuthAction) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    QodeinElevatedCard(
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(SpacingTokens.md)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeLogo(
                size = QodeLogoSize.Large,
                style = QodeLogoStyle.Default,
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(bottom = SpacingTokens.sm),
            )

            Text(
                text = stringResource(R.string.sign_in_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = stringResource(R.string.sign_in_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            QodeGoogleSignInButton(
                onClick = {
                    onAction(AuthAction.AuthWithGoogleClicked(context))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SpacingTokens.md),
                isLoading = isLoading,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
            ) {
                Text(
                    text = stringResource(R.string.terms_first_line),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    QodeTextButton(
                        text = stringResource(R.string.terms_of_service),
                        onClick = {
                            onAction(AuthAction.LegalDocumentClicked(DocumentType.TermsOfService))
                        },
                        showUnderline = false,
                    )

                    HorizontalDivider()

                    QodeTextButton(
                        text = stringResource(R.string.privacy_policy),
                        onClick = {
                            onAction(AuthAction.LegalDocumentClicked(DocumentType.PrivacyPolicy))
                        },
                        showUnderline = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthErrorMessage(
    error: OperationError,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeinElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(ShapeTokens.Corner.medium),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = error.asUiText(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
            )

            QodeinIconButton(
                onClick = onDismiss,
                icon = QodeActionIcons.Close,
                contentDescription = stringResource(CoreUiR.string.cd_close),
                size = ButtonSize.Small,
            )
        }
    }
}

// MARK: - Previews

@ThemePreviews
@Composable
private fun AuthIdleStatePreview() {
    QodeTheme {
        AuthScreen(
            context = LocalContext.current,
            onBackClick = {},
            authState = AuthUiState(),
            legalDocumentState = LegalDocumentUiState.Closed,
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun AuthLoadingStatePreview() {
    QodeTheme {
        AuthScreen(
            context = LocalContext.current,
            onBackClick = {},
            authState = AuthUiState(isSigningIn = true),
            legalDocumentState = LegalDocumentUiState.Closed,
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun AuthErrorStatePreview() {
    QodeTheme {
        AuthScreen(
            context = LocalContext.current,
            onBackClick = {},
            authState = AuthUiState(error = SystemError.Offline),
            legalDocumentState = LegalDocumentUiState.Closed,
            onAction = {},
        )
    }
}
