package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.icon.NavigationIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.R
import com.qodein.core.ui.R as CoreUiR

@Composable
fun WizardController(
    canGoNext: Boolean,
    canGoBack: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    canSubmit: Boolean = false,
    onSubmit: (() -> Unit)? = null
) {
    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val imePadding = WindowInsets.ime.asPaddingValues()

    val bottomPadding = if (imePadding.calculateBottomPadding() > 0.dp) {
        SpacingTokens.sm + navigationBarsPadding.calculateBottomPadding() // Compact spacing when keyboard is open
    } else {
        SpacingTokens.xl + navigationBarsPadding.calculateBottomPadding() // Normal floating when keyboard is closed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SizeTokens.Controller.containerHeight)
            .padding(bottom = bottomPadding),
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
                // MARK: - Back button
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
                        imageVector = ActionIcons.Previous,
                        contentDescription = stringResource(CoreUiR.string.ui_back),
                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                    )
                }

                IconButton(
                    onClick = if (canGoNext) {
                        onNext
                    } else if (canSubmit && onSubmit != null) {
                        onSubmit
                    } else {
                        onNext
                    },
                    enabled = (canGoNext || (canSubmit && onSubmit != null)) && !isLoading,
                    modifier = Modifier.size(SizeTokens.IconButton.sizeLarge).weight(2f),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
                            imageVector = if (canGoNext) {
                                NavigationIcons.ChevronRight
                            } else if (canSubmit && onSubmit != null) {
                                ActionIcons.Check
                            } else {
                                NavigationIcons.ChevronRight
                            },
                            contentDescription = if (canGoNext) {
                                stringResource(R.string.action_continue)
                            } else if (canSubmit && onSubmit != null) {
                                stringResource(R.string.action_submit)
                            } else {
                                stringResource(R.string.action_continue)
                            },
                            modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun WizardControllerPreview() {
    QodeTheme {
        WizardController(
            canGoNext = true,
            canGoBack = true,
            onNext = {},
            onPrevious = {},
        )
    }
}
