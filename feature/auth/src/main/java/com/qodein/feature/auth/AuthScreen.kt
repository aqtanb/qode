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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.simon.xmaterialccp.data.CountryData
import com.simon.xmaterialccp.data.utils.getDefaultLangCode
import com.simon.xmaterialccp.data.utils.getLibCountries

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onSendVerificationCode: (String) -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }

    // FIXED: Properly manage selected country state
    var selectedCountry by remember {
        mutableStateOf<CountryData?>(
            try {
                getLibCountries().single { it.countryCode == getDefaultLangCode(context) }
            } catch (_: Exception) {
                getLibCountries().find { it.countryCode == "KZ" } ?: getLibCountries().first()
            },
        )
    }

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
                        horizontal = QodeSpacing.lg,
                        vertical = QodeSpacing.xxxl,
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
                            .padding(QodeSpacing.md)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
                    ) {
                        // Logo
                        QodeLogo(
                            size = QodeLogoSize.Large,
                            style = QodeLogoStyle.Default,
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(bottom = QodeSpacing.sm),
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
                            modifier = Modifier.padding(bottom = QodeSpacing.sm),
                        )

                        // Phone section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = QodeSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
                        ) {
                            // Phone number label with enhanced styling
                            Text(
                                text = stringResource(R.string.phone_number_label),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = QodeSpacing.xs),
                            )
                            QodePhoneInput(
                                phoneNumber = phoneNumber,
                                selectedCountry = selectedCountry, // FIXED: Now uses actual state
                                validationState = PhoneValidationState.Idle,
                                onPhoneNumberChange = { phoneNumber = it },
                                onCountryClick = { country ->
                                    selectedCountry = country // FIXED: Actually updates the state
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = QodeSpacing.sm),
                            )
                        }

                        // Send verification code button
                        QodeButton(
                            text = stringResource(R.string.send_verification_code),
                            onClick = {
                                val fullPhoneNumber = "${selectedCountry?.countryPhoneCode ?: "+7"}$phoneNumber"
                                onSendVerificationCode(fullPhoneNumber)
                            },
                            variant = QodeButtonVariant.Primary,
                            size = QodeButtonSize.Large,
                            leadingIcon = QodeActionIcons.Send,
                            modifier = Modifier.fillMaxWidth().padding(top = QodeSpacing.sm),
                        )

                        // Divider with "Or continue with" text
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = QodeSpacing.lg),
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
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Forgot password link
                        QodeTextButton(
                            text = stringResource(R.string.forgot_password),
                            onClick = onForgotPassword,
                            style = QodeTextButtonStyle.Primary,
                            showUnderline = true,
                            modifier = Modifier.padding(top = QodeSpacing.sm),
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = QodeSpacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(QodeSpacing.xs),
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
                                    onClick = onTermsClick,
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
                                    onClick = onPrivacyClick,
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
