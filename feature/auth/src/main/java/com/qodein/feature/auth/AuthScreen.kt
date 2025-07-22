package com.qodein.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
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
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.component.PhoneValidationState
import com.qodein.core.ui.component.QodePhoneInput
import com.qodein.feature.auth.R

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onSendVerificationCode: (String) -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    var phoneNumber by remember { mutableStateOf("") }

    QodeGradientBackground(
        style = QodeGradientStyle.Primary,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Main content card
                QodeCard(
                    variant = QodeCardVariant.Elevated,
                    modifier = Modifier
                        .padding(
                            top = QodeSpacing.xxxl + QodeSpacing.lg,
                            start = QodeSpacing.lg,
                            end = QodeSpacing.lg,
                        )
                        .fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(QodeSpacing.lg)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Logo with purple background
                        QodeLogo(
                            size = QodeLogoSize.Large,
                            style = QodeLogoStyle.Default,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = QodeSpacing.md),
                        )

                        // Title
                        Text(
                            text = stringResource(R.string.sign_in_title),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = QodeSpacing.xs),
                        )

                        // Subtitle
                        Text(
                            text = stringResource(R.string.sign_in_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = QodeSpacing.lg),
                        )

                        // Phone number label
                        Text(
                            text = stringResource(R.string.phone_number_label),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = QodeSpacing.xs),
                        )

                        // Phone input
                        QodePhoneInput(
                            phoneNumber = phoneNumber,
                            selectedCountry = null,
                            validationState = PhoneValidationState.Idle,
                            onPhoneNumberChange = { phoneNumber = it },
                            onCountryClick = { /* TODO: Handle country picker */ },
                            placeholder = "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = QodeSpacing.lg),
                        )

                        // Send verification code button
                        QodeButton(
                            text = stringResource(R.string.send_verification_code),
                            onClick = { onSendVerificationCode(phoneNumber) },
                            variant = QodeButtonVariant.Primary,
                            size = QodeButtonSize.Large,
                            leadingIcon = QodeActionIcons.Send,

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = QodeSpacing.lg),
                        )

                        // Divider with "Or continue with" text
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = QodeSpacing.md),
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
                                modifier = Modifier.padding(horizontal = QodeSpacing.md),
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }

                        // Google sign in button
                        QodeGoogleSignInButton(
                            onClick = onGoogleSignIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = QodeSpacing.lg),
                        )

                        // Forgot password link
                        QodeTextButton(
                            text = stringResource(R.string.forgot_password),
                            onClick = onForgotPassword,
                            style = QodeTextButtonStyle.Primary,
                            showUnderline = true,
                            modifier = Modifier.padding(bottom = QodeSpacing.md),
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QodeSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Sign up text
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.dont_have_account),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                        QodeTextButton(
                            text = stringResource(R.string.sign_up),
                            onClick = onSignUp,
                            style = QodeTextButtonStyle.Primary,
                        )
                    }

                    // Terms and privacy
                    val termsAndPrivacyText = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            ),
                        ) {
                            append("By continuing, you agree to our ")
                        }
                        withStyle(
                            SpanStyle(
                                color = Color.White,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            ),
                        ) {
                            append(stringResource(R.string.terms_of_service))
                        }
                        withStyle(
                            SpanStyle(
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            ),
                        ) {
                            append(" and ")
                        }
                        withStyle(
                            SpanStyle(
                                color = Color.White,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            ),
                        ) {
                            append(stringResource(R.string.privacy_policy))
                        }
                    }

                    Text(
                        text = termsAndPrivacyText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = QodeSpacing.md),
                    )
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
