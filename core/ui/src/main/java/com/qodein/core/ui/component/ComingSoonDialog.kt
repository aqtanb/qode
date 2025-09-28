package com.qodein.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R

/**
 * A reusable "Coming Soon" dialog component for features under development.
 *
 * Features:
 * - Modern Material 3 design with QodeTheme integration
 * - Official Telegram brand styling
 * - Smooth animations using design tokens
 * - Accessibility support with proper semantic markup
 * - Responsive design for all screen sizes
 * - Customizable title and description
 *
 * Architecture Note: This component handles only UI presentation. URL opening and business
 * logic should be handled by the calling component to maintain proper separation of concerns.
 *
 * @param onDismiss Called when the dialog should be dismissed
 * @param onTelegramClick Called when the Telegram button is clicked
 * @param title The title text to display. Defaults to "Coming Soon" from string resources
 * @param description The description text to display. Defaults to standard coming soon message
 * @param modifier Modifier to be applied to the dialog content
 */
@Composable
fun ComingSoonDialog(
    onDismiss: () -> Unit,
    onTelegramClick: () -> Unit,
    title: String = stringResource(R.string.coming_soon_title),
    description: String = stringResource(R.string.coming_soon_description),
    modifier: Modifier = Modifier
) {
    // Smooth entrance animation
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = AnimationTokens.Spec.medium(),
        label = "dialog_alpha",
    )

    // Use provided description for accessibility
    val dialogDescription = description

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        QodeCard(
            modifier = modifier
                .fillMaxWidth()
                .alpha(alpha)
                .padding(SpacingTokens.md)
                .semantics {
                    contentDescription = dialogDescription
                },
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Close button in top right
                QodeIconButton(
                    onClick = onDismiss,
                    icon = QodeActionIcons.Close,
                    contentDescription = stringResource(R.string.close),
                    variant = QodeButtonVariant.Text,
                    size = QodeButtonSize.Medium,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(SpacingTokens.sm),
                )

                // Main content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                ) {
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(end = SizeTokens.IconButton.sizeSmall), // Add padding to avoid close button
                    )

                    // Description
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = SpacingTokens.sm),
                    )

                    Spacer(modifier = Modifier.height(SpacingTokens.sm))

                    // Full-width Telegram button
                    QodeTelegramFollowButton(
                        onClick = onTelegramClick,
                        text = stringResource(R.string.coming_soon_telegram_button),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// MARK: - Previews

@Preview(name = "Coming Soon Dialog", showBackground = true)
@Composable
private fun QodeComingSoonDialogPreview() {
    QodeTheme {
        ComingSoonDialog(
            onDismiss = {},
            onTelegramClick = {},
        )
    }
}

@Preview(name = "Coming Soon Dialog - Dark Theme", showBackground = true)
@Composable
private fun QodeComingSoonDialogDarkPreview() {
    QodeTheme(darkTheme = true) {
        ComingSoonDialog(
            onDismiss = {},
            onTelegramClick = {},
        )
    }
}
