package com.qodein.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeGoogleSignInButton
import com.qodein.core.designsystem.component.QodeGradientBackground
import com.qodein.core.designsystem.component.QodeGradientStyle
import com.qodein.core.designsystem.component.QodeLogo
import com.qodein.core.designsystem.component.QodeLogoSize
import com.qodein.core.designsystem.component.QodeLogoStyle
import com.qodein.core.designsystem.component.QodeTextButton
import com.qodein.core.designsystem.component.QodeTextButtonStyle
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.PhoneValidationState
import com.qodein.core.ui.component.QodePhoneInput

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    AuthContent(
        modifier = modifier,
        onAction = viewModel::onAction,
        uiState = uiState,
    )
}

@Composable
fun AuthContent(
    modifier: Modifier = Modifier,
    onAction: (AuthAction) -> Unit,
    uiState: AuthUiState
) {
    QodeGradientBackground(
        style = QodeGradientStyle.Primary,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = SpacingTokens.lg,
                        vertical = SpacingTokens.xxxl,
                    ),
                verticalArrangement = Arrangement.Center,
            ) {
                // Single card with all content
                QodeCard(
                    variant = QodeCardVariant.Elevated,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(SpacingTokens.md)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    ) {
                        // Logo
                        QodeLogo(
                            size = QodeLogoSize.Large,
                            style = QodeLogoStyle.Default,
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(bottom = SpacingTokens.sm),
                        )

                        // Title
                        Text(
                            text = stringResource(R.string.sign_in_title),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        // Subtitle
                        Text(
                            text = stringResource(R.string.sign_in_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = SpacingTokens.md),
                        )

                        // Phone section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = SpacingTokens.sm),
                        ) {
                            // Phone number label with enhanced styling
                            Text(
                                text = stringResource(R.string.phone_number_label),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = SpacingTokens.xs, bottom = SpacingTokens.sm),
                            )
                            QodePhoneInput(
                                phoneNumber = uiState.phoneNumber,
                                selectedCountry = uiState.selectedCountry,
                                validationState = PhoneValidationState.Idle,
                                onPhoneNumberChange = { onAction(AuthAction.PhoneNumberChanged(it)) },
                                onCountryPickerClick = {
                                    // TODO: Handle country picker click
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        // Send verification code button
                        QodeButton(
                            text = stringResource(R.string.send_verification_code),
                            onClick = {
                                // TODO: Handle button click
                            },
                            variant = QodeButtonVariant.Primary,
                            size = QodeButtonSize.Large,
                            leadingIcon = QodeActionIcons.Send,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Divider with "Or continue with" text
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = SpacingTokens.lg),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Text(
                                text = stringResource(R.string.or_continue_with),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = SpacingTokens.md),
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }

                        // Google sign in button
                        QodeGoogleSignInButton(
                            onClick = {
                                // TODO: Handle button click
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = SpacingTokens.lg),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        ) {
                            // First line
                            Text(
                                text = stringResource(R.string.terms_first_line),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )

                            // Second line with clickable links
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
    }
}

@Preview(name = "Auth Screen", showSystemUi = true)
@Composable
private fun AuthScreenPreview() {
    QodeTheme {
        AuthScreen()
    }
}

@Preview(name = "Auth Screen - Dark", showSystemUi = true)
@Composable
private fun AuthScreenDarkPreview() {
    QodeTheme(darkTheme = true) {
        AuthScreen()
    }
}
