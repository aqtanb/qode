package com.qodein.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeBorder
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSize
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Social login button types
 */
enum class SocialProvider {
    Google,
    Apple,
    Facebook
}

/**
 * Social login button component following platform guidelines
 */
@Composable
fun QodeSocialButton(
    provider: SocialProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    text: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale = if (isPressed && enabled && !loading) 0.96f else 1f

    val (backgroundColor, contentColor, borderColor, buttonText, iconColor) = when (provider) {
        SocialProvider.Google -> {
            SocialButtonColors(
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                borderColor = MaterialTheme.colorScheme.outline,
                text = text ?: "Continue with Google",
                iconColor = Color.Unspecified, // Will use original Google colors
            )
        }
        SocialProvider.Apple -> {
            SocialButtonColors(
                backgroundColor = Color.Black,
                contentColor = Color.White,
                borderColor = Color.Black,
                text = text ?: "Continue with Apple",
                iconColor = Color.White,
            )
        }
        SocialProvider.Facebook -> {
            SocialButtonColors(
                backgroundColor = Color(0xFF1877F2),
                contentColor = Color.White,
                borderColor = Color(0xFF1877F2),
                text = text ?: "Continue with Facebook",
                iconColor = Color.White,
            )
        }
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(QodeSize.buttonHeightLarge)
            .scale(scale),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(QodeCorners.md),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.6f),
            disabledContentColor = contentColor.copy(alpha = 0.38f),
        ),
        border = BorderStroke(
            width = QodeBorder.thin,
            color = if (enabled && !loading) borderColor else borderColor.copy(alpha = 0.38f),
        ),
        contentPadding = PaddingValues(
            horizontal = SpacingTokens.lg,
            vertical = SpacingTokens.md,
        ),
        interactionSource = interactionSource,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(QodeSize.iconMedium),
                strokeWidth = 2.dp,
                color = contentColor,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Provider icon
                SocialIcon(
                    provider = provider,
                    tint = iconColor,
                    modifier = Modifier.size(QodeSize.iconMedium),
                )

                Spacer(modifier = Modifier.width(SpacingTokens.md))

                // Button text
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                )
            }
        }
    }
}

/**
 * Social provider icon component
 */
@Composable
private fun SocialIcon(
    provider: SocialProvider,
    tint: Color,
    modifier: Modifier = Modifier
) {
    when (provider) {
        SocialProvider.Google -> {
            // Google "G" icon with proper styling
            Text(
                text = "G",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = Color(0xFF4285F4), // Google Blue
                modifier = modifier,
            )
        }
        SocialProvider.Apple -> {
            // Apple icon
            Text(
                text = "",
                style = MaterialTheme.typography.titleLarge,
                color = tint,
                modifier = modifier,
            )
        }
        SocialProvider.Facebook -> {
            // Facebook "f" icon
            Text(
                text = "f",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = tint,
                modifier = modifier,
            )
        }
    }
}

/**
 * Data class for social button styling
 */
private data class SocialButtonColors(
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val text: String,
    val iconColor: Color
)

/**
 * Divider with text component (e.g., "Or continue with")
 */
@Composable
fun QodeDividerWithText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    dividerColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Left divider
        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = QodeBorder.thin,
            color = dividerColor,
        )

        // Text
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = SpacingTokens.md),
        )

        // Right divider
        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = QodeBorder.thin,
            color = dividerColor,
        )
    }
}

// Previews
@Preview(name = "Social Buttons", showBackground = true)
@Composable
private fun QodeSocialButtonPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Google button
            QodeSocialButton(
                provider = SocialProvider.Google,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )

            // Apple button
            QodeSocialButton(
                provider = SocialProvider.Apple,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )

            // Facebook button
            QodeSocialButton(
                provider = SocialProvider.Facebook,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )

            // Loading state
            QodeSocialButton(
                provider = SocialProvider.Google,
                onClick = {},
                loading = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Disabled state
            QodeSocialButton(
                provider = SocialProvider.Google,
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "Divider with Text", showBackground = true)
@Composable
private fun QodeDividerWithTextPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            QodeDividerWithText(text = "Or continue with")
            QodeDividerWithText(text = "Or")
            QodeDividerWithText(text = "Sign up with")
        }
    }
}
