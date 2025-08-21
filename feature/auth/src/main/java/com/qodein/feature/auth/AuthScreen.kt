package com.qodein.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeHeroGradient
import com.qodein.core.designsystem.component.QodeLogo
import com.qodein.core.designsystem.component.QodeLogoSize
import com.qodein.core.designsystem.component.QodeLogoStyle
import com.qodein.core.designsystem.component.QodeTextButton
import com.qodein.core.designsystem.component.QodeTextButtonStyle
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.ComponentPreviews
import com.qodein.core.ui.DevicePreviews
import com.qodein.core.ui.FontScalePreviews
import com.qodein.core.ui.MobilePreviews
import com.qodein.core.ui.TabletPreviews
import com.qodein.core.ui.ThemePreviews
import com.qodein.core.ui.component.QodeActionErrorCard
import com.qodein.core.ui.component.QodeGoogleSignInButton
import com.qodein.core.ui.error.toLocalizedMessage
import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.common.result.suggestedAction

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToTermsOfService: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {}
) {
    // Track screen view
    TrackScreenViewEvent(screenName = "Auth")

    val state by viewModel.state.collectAsState()

    // Handle events
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.SignedIn -> onNavigateToHome()
                is AuthEvent.TermsOfServiceRequested -> onNavigateToTermsOfService()
                is AuthEvent.PrivacyPolicyRequested -> onNavigateToPrivacyPolicy()
            }
        }
    }

    AuthContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::handleAction,
    )
}

@Composable
fun AuthContent(
    modifier: Modifier = Modifier,
    state: AuthUiState,
    onAction: (AuthAction) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        QodeHeroGradient()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SpacingTokens.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (state) {
                is AuthUiState.Error -> {
                    // Show action-based error card with intelligent action mapping
                    QodeActionErrorCard(
                        message = state.errorType.toLocalizedMessage(),
                        errorAction = state.errorType.suggestedAction(),
                        onActionClicked = { onAction(AuthAction.RetryClicked) },
                        onDismiss = { onAction(AuthAction.DismissErrorClicked) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                else -> {
                    // Show sign-in card for all other states (Idle, Loading)
                    AuthSignInCard(
                        state = state,
                        onAction = onAction,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthSignInCard(
    state: AuthUiState,
    onAction: (AuthAction) -> Unit,
    modifier: Modifier = Modifier
) {
    QodeCard(
        variant = QodeCardVariant.Elevated,
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
                    onAction(AuthAction.SignInWithGoogleClicked)
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = SpacingTokens.md),
                isLoading = state is AuthUiState.Loading,
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
                            onAction(AuthAction.TermsOfServiceClicked)
                        },
                        style = QodeTextButtonStyle.Primary,
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
                            onAction(AuthAction.PrivacyPolicyClicked)
                        },
                        style = QodeTextButtonStyle.Primary,
                        showUnderline = true,
                    )
                }
            }
        }
    }
}

// MARK: - Preview Functions

/**
 * Preview-optimized version for AuthScreen with proper state handling
 */
@Composable
private fun AuthScreenPreview(
    state: AuthUiState,
    modifier: Modifier = Modifier
) {
    AuthContent(
        state = state,
        onAction = {}, // Empty for previews
        modifier = modifier,
    )
}

/**
 * Comprehensive device previews following NIA patterns
 */
@DevicePreviews
@Composable
fun AuthScreenDevicePreviews() {
    QodeTheme {
        AuthScreenPreview(
            state = AuthUiState.Idle,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Theme variations preview
 */
@ThemePreviews
@Composable
fun AuthScreenThemePreviews() {
    QodeTheme {
        AuthScreenPreview(
            state = AuthUiState.Idle,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Font scale accessibility testing
 */
@FontScalePreviews
@Composable
fun AuthScreenFontScalePreviews() {
    QodeTheme {
        AuthScreenPreview(
            state = AuthUiState.Idle,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Mobile-specific layouts
 */
@MobilePreviews
@Composable
fun AuthScreenMobilePreviews() {
    QodeTheme {
        AuthScreenPreview(
            state = AuthUiState.Idle,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Tablet-optimized layouts
 */
@TabletPreviews
@Composable
fun AuthScreenTabletPreviews() {
    QodeTheme {
        AuthScreenPreview(
            state = AuthUiState.Idle,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Loading state preview following enterprise patterns
 */
@ComponentPreviews
@Composable
fun AuthScreenLoadingStatePreview() {
    QodeTheme {
        AuthScreenPreview(
            state = AuthUiState.Loading,
        )
    }
}

/**
 * Error state preview with proper error handling UI
 */
@ComponentPreviews
@Composable
fun AuthScreenErrorStatePreview() {
    QodeTheme {
        AuthScreenPreview(
            state = AuthUiState.Error(
                errorType = ErrorType.AUTH_USER_CANCELLED,
                isRetryable = false,
                shouldShowSnackbar = false,
                errorCode = "AUTH_001",
            ),
        )
    }
}

/**
 * Network error state preview
 */
@ComponentPreviews
@Composable
fun AuthScreenNetworkErrorPreview() {
    QodeTheme {
        AuthScreenPreview(
            state = AuthUiState.Error(
                errorType = ErrorType.NETWORK_GENERAL,
                isRetryable = true,
                shouldShowSnackbar = false,
                errorCode = "NET_001",
            ),
        )
    }
}
