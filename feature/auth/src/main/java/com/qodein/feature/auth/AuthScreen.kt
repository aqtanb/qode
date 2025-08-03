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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeGoogleSignInButton
import com.qodein.core.designsystem.component.QodeLogo
import com.qodein.core.designsystem.component.QodeLogoSize
import com.qodein.core.designsystem.component.QodeLogoStyle
import com.qodein.core.designsystem.component.QodeTextButton
import com.qodein.core.designsystem.component.QodeTextButtonStyle
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SpacingTokens.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        QodeCard(
            variant = QodeCardVariant.Elevated,
            shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
            modifier = modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = modifier
                    .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.xl)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                QodeLogo(
                    size = QodeLogoSize.Large,
                    style = QodeLogoStyle.Default,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    modifier = modifier.padding(bottom = SpacingTokens.sm),
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = SpacingTokens.xl),
                    isLoading = state.isLoading,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
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
                                // TODO: Handle button click
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
                                // TODO: Handle button click
                            },
                            style = QodeTextButtonStyle.Primary,
                            showUnderline = true,
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Auth Screen", showSystemUi = true)
@Composable
private fun AuthScreenPreview() {
    QodeTheme {
        AuthContent(
            onAction = {},
            state = AuthUiState(),
        )
    }
}
