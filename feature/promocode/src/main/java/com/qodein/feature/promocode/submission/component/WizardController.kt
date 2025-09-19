package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.R

@Composable
fun WizardController(
    canGoNext: Boolean,
    canGoBack: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    nextButtonText: String = stringResource(R.string.action_continue)
) {
    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SizeTokens.Controller.containerHeight)
            .padding(bottom = SpacingTokens.xl + navigationBarsPadding.calculateBottomPadding()),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .width(SizeTokens.Controller.pillWidth)
                .height(SizeTokens.Controller.pillHeight)
                .clip(RoundedCornerShape(ShapeTokens.Corner.full)),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = ElevationTokens.none,
            tonalElevation = ElevationTokens.small,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = SpacingTokens.sm,
                        vertical = SpacingTokens.xs,
                    ),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onPrevious,
                    enabled = canGoBack,
                    modifier = Modifier.size(SizeTokens.IconButton.sizeLarge).weight(1f),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = QodeActionIcons.Previous,
                        contentDescription = stringResource(R.string.action_back),
                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                    )
                }

                // Next button - icon button with primary styling
                IconButton(
                    onClick = onNext,
                    enabled = canGoNext && !isLoading,
                    modifier = Modifier.size(SizeTokens.IconButton.sizeLarge).weight(2f),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = QodeActionIcons.Next,
                            contentDescription = nextButtonText,
                            modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Wizard Controller - Normal", showBackground = true)
@Composable
private fun WizardControllerPreview() {
    QodeTheme {
        WizardController(
            canGoNext = true,
            canGoBack = true,
            nextButtonText = "Continue",
            onNext = {},
            onPrevious = {},
        )
    }
}

@Preview(name = "Wizard Controller - First Step", showBackground = true)
@Composable
private fun WizardControllerFirstStepPreview() {
    QodeTheme {
        WizardController(
            canGoNext = true,
            canGoBack = false,
            nextButtonText = "Get Started",
            onNext = {},
            onPrevious = {},
        )
    }
}

@Preview(name = "Wizard Controller - Loading", showBackground = true)
@Composable
private fun WizardControllerLoadingPreview() {
    QodeTheme {
        WizardController(
            canGoNext = true,
            canGoBack = true,
            isLoading = true,
            nextButtonText = "Submit",
            onNext = {},
            onPrevious = {},
        )
    }
}

@Preview(name = "Wizard Controller - Disabled", showBackground = true)
@Composable
private fun WizardControllerDisabledPreview() {
    QodeTheme {
        WizardController(
            canGoNext = false,
            canGoBack = true,
            nextButtonText = "Continue",
            onNext = {},
            onPrevious = {},
        )
    }
}
