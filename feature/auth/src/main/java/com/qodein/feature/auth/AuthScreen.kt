package com.qodein.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeLogo
import com.qodein.core.designsystem.component.QodeLogoSize
import com.qodein.core.designsystem.component.QodeLogoStyle
import com.qodein.core.designsystem.component.QodeTextButton
import com.qodein.core.designsystem.component.QodeinElevatedCard
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.component.QodeGoogleSignInButton
import com.qodein.feature.auth.component.AuthTopAppBar
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError

@Composable
fun AuthRoute(
    isDarkTheme: Boolean,
    onNavigateToHome: () -> Unit,
    onNavigateToTermsOfService: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "Auth")

    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.SignedIn -> onNavigateToHome()
            }
        }
    }

    AuthScreen(
        onBackClick = onNavigateToHome,
        state = uiState,
        onAction = viewModel::handleAction,
        isDarkTheme = isDarkTheme,
        modifier = modifier,
    )
}

@Composable
private fun AuthScreen(
    onBackClick: () -> Unit,
    state: AuthUiState,
    onAction: (SignInAction) -> Unit,
    isDarkTheme: Boolean,
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
                .padding(horizontal = SpacingTokens.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (state) {
                is AuthUiState.Idle -> {
                    AuthIdleState(
                        onAction = onAction,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is AuthUiState.Loading -> {
                    AuthLoadingState(
                        onAction = onAction,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is AuthUiState.Error -> {
                    AuthErrorState(
                        error = state.errorType,
                        onAction = onAction,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// MARK: - State Composables

@Composable
private fun AuthIdleState(
    onAction: (SignInAction) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    AuthSignInCard(
        onAction = onAction,
        isDarkTheme = isDarkTheme,
        isLoading = false,
        modifier = modifier,
    )
}

@Composable
private fun AuthLoadingState(
    onAction: (SignInAction) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    AuthSignInCard(
        onAction = onAction,
        isDarkTheme = isDarkTheme,
        isLoading = true,
        modifier = modifier,
    )
}

@Composable
private fun AuthErrorState(
    error: OperationError,
    onAction: (SignInAction) -> Unit,
    modifier: Modifier = Modifier
) {
    QodeErrorCard(
        error = error,
        onRetry = { onAction(SignInAction.RetryClicked) },
        onDismiss = { onAction(SignInAction.DismissErrorClicked) },
        modifier = modifier,
    )
}

@Composable
private fun AuthSignInCard(
    onAction: (SignInAction) -> Unit,
    isDarkTheme: Boolean,
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
                    onAction(SignInAction.SignInWithGoogleClicked)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SpacingTokens.md),
                isLoading = isLoading,
                isDarkTheme = isDarkTheme,
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

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QodeTextButton(
                        text = stringResource(R.string.terms_of_service),
                        onClick = {
                            onAction(SignInAction.TermsOfServiceClicked)
                        },
                        showUnderline = true,
                    )

                    Text(
                        text = " ${stringResource(R.string.and)} ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    QodeTextButton(
                        text = stringResource(R.string.privacy_policy),
                        onClick = {
                            onAction(SignInAction.PrivacyPolicyClicked)
                        },
                        showUnderline = true,
                    )
                }
            }
        }
    }
}

// MARK: - Previews

@ThemePreviews
@Composable
private fun AuthIdleStatePreview() {
    QodeTheme {
        AuthScreen(
            onBackClick = {},
            state = AuthUiState.Idle,
            onAction = {},
            isDarkTheme = false,
        )
    }
}

@ThemePreviews
@Composable
private fun AuthLoadingStatePreview() {
    QodeTheme {
        AuthScreen(
            onBackClick = {},
            state = AuthUiState.Loading,
            onAction = {},
            isDarkTheme = false,
        )
    }
}

@ThemePreviews
@Composable
private fun AuthErrorStatePreview() {
    QodeTheme {
        AuthScreen(
            onBackClick = {},
            state = AuthUiState.Error(
                errorType = SystemError.Offline,
            ),
            onAction = {},
            isDarkTheme = false,
        )
    }
}
