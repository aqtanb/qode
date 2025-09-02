package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.PageIndicator
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.ProgressiveStep

@Composable
fun BottomController(
    currentStep: ProgressiveStep,
    canProceed: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            animationSpec = tween(300),
            initialOffsetY = { it },
        ),
        exit = slideOutVertically(
            animationSpec = tween(300),
            targetOffsetY = { it },
        ),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = ElevationTokens.large,
                    shape = RoundedCornerShape(
                        topStart = ShapeTokens.Corner.large,
                        topEnd = ShapeTokens.Corner.large,
                    ),
                    clip = false,
                )
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(
                        topStart = ShapeTokens.Corner.large,
                        topEnd = ShapeTokens.Corner.large,
                    ),
                )
                .padding(SpacingTokens.lg),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Page indicator
                PageIndicator(
                    currentPage = currentStep.stepNumber - 1,
                    totalPages = ProgressiveStep.entries.size,
                    modifier = modifier.fillMaxWidth(),
                    activeColor = MaterialTheme.colorScheme.primary,
                    inactiveColor = MaterialTheme.colorScheme.outline,
                )

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                ) {
                    // Back button
                    if (!currentStep.isFirst) {
                        QodeButton(
                            onClick = onPrevious,
                            text = stringResource(R.string.action_back),
                            variant = QodeButtonVariant.Outlined,
                            size = QodeButtonSize.Medium,
                            leadingIcon = QodeActionIcons.Back,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Next/Submit button
                    QodeButton(
                        onClick = onNext,
                        text = if (currentStep.isLast) {
                            stringResource(R.string.action_submit)
                        } else {
                            stringResource(R.string.action_continue)
                        },
                        variant = QodeButtonVariant.Primary,
                        size = QodeButtonSize.Medium,
                        enabled = canProceed,
                        trailingIcon = if (!currentStep.isLast) QodeActionIcons.Forward else null,
                        modifier = Modifier.weight(if (currentStep.isFirst) 1f else 2f),
                    )
                }
            }
        }
    }
}

@Preview(name = "Bottom Controller - First Step", showBackground = true)
@Composable
private fun BottomControllerFirstStepPreview() {
    QodeTheme {
        BottomController(
            currentStep = ProgressiveStep.SERVICE,
            canProceed = true,
            onNext = {},
            onPrevious = {},
        )
    }
}

@Preview(name = "Bottom Controller - Middle Step", showBackground = true)
@Composable
private fun BottomControllerMiddleStepPreview() {
    QodeTheme {
        BottomController(
            currentStep = ProgressiveStep.PROMO_CODE,
            canProceed = false,
            onNext = {},
            onPrevious = {},
        )
    }
}

@Preview(name = "Bottom Controller - Last Step", showBackground = true)
@Composable
private fun BottomControllerLastStepPreview() {
    QodeTheme {
        BottomController(
            currentStep = ProgressiveStep.PROMO_CODE,
            canProceed = true,
            onNext = {},
            onPrevious = {},
        )
    }
}
