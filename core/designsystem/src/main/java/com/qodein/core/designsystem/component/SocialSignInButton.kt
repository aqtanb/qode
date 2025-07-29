package com.qodein.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.R
import com.qodein.core.designsystem.theme.OpacityTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Simple Google Sign-In button using official Google drawables
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
    val contentDescription = stringResource(R.string.continue_with_google)

    Box(
        modifier = modifier
            .height(SizeTokens.Button.heightLarge) // 48dp - slightly bigger than medium
            .wrapContentWidth(), // Let it size naturally
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = ShapeTokens.Border.thin,
            )
        } else {
            Image(
                painter = painterResource(
                    if (isLight) {
                        R.drawable.ic_google_continue_light
                    } else {
                        R.drawable.ic_google_continue_dark
                    },
                ),
                contentDescription = contentDescription,
                modifier = Modifier
                    .height(SizeTokens.Button.heightLarge) // Match the box height
                    .wrapContentWidth() // Natural width based on aspect ratio
                    .alpha(if (enabled) 1f else OpacityTokens.DISABLED)
                    .clickable(
                        enabled = enabled,
                        onClick = onClick,
                        role = Role.Button,
                    )
                    .semantics {
                        role = Role.Button
                        this.contentDescription = contentDescription
                    },
                contentScale = ContentScale.FillHeight, // Fit height, natural width
            )
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
