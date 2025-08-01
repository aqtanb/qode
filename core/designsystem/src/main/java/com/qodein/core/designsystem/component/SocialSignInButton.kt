package com.qodein.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.R
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Google Sign-In button following Google's brand guidelines
 * Features a 40x40 Google logo with "Continue with Google" text
 * Automatically switches between light/dark theme variants
 *
 * @param onClick Called when the button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled
 * @param isLoading Whether the button is in loading state
 */
@Composable
fun QodeGoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val isLight = !isSystemInDarkTheme()

    // Google brand colors and styles following their guidelines
    val buttonBackgroundColor = if (isLight) Color.White else Color(0xFF131314)
    val buttonTextColor = if (isLight) Color(0xFF1F1F1F) else Color(0xFFFFFFFF)
    val borderColor = if (isLight) Color(0xFF747775) else Color(0xFF8E918F)

    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(), // Google recommended height
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(8.dp), // Slightly rounded corners per Google guidelines
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonBackgroundColor,
            contentColor = buttonTextColor,
            disabledContainerColor = buttonBackgroundColor.copy(alpha = 0.12f),
            disabledContentColor = buttonTextColor.copy(alpha = 0.38f),
        ),
        border = BorderStroke(1.dp, borderColor.copy(alpha = if (enabled) 1f else 0.38f)),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 1.dp,
            disabledElevation = 0.dp,
        ),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = "Google Logo",
            )

            Spacer(modifier = Modifier.width(SpacingTokens.md))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                    color = buttonTextColor,
                    strokeWidth = 2.dp,
                )
            } else {
                // "Continue with Google" text
                Text(
                    text = stringResource(R.string.continue_with_google),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = buttonTextColor,
                )
            }
        }
    }
}

// MARK: - Previews

@Preview(name = "Google Button - Light", showBackground = true)
@Composable
private fun QodeGoogleSignInButtonLightPreview() {
    QodeTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            QodeGoogleSignInButton(onClick = {})

            QodeGoogleSignInButton(
                onClick = {},
                enabled = false,
            )

            QodeGoogleSignInButton(
                onClick = {},
                isLoading = true,
            )
        }
    }
}

@Preview(name = "Google Button - Dark", showBackground = true)
@Composable
private fun QodeGoogleSignInButtonDarkPreview() {
    QodeTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            QodeGoogleSignInButton(onClick = {})

            QodeGoogleSignInButton(
                onClick = {},
                enabled = false,
            )

            QodeGoogleSignInButton(
                onClick = {},
                isLoading = true,
            )
        }
    }
}

@Preview(name = "Google Button - In Form", showBackground = true)
@Composable
private fun QodeGoogleSignInButtonInFormPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            QodeGoogleSignInButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(), // This will center it in the full width
            )
        }
    }
}
