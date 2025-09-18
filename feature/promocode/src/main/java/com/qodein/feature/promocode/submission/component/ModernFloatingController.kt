package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.ProgressiveStep

@Composable
fun ModernFloatingController(
    currentStep: ProgressiveStep,
    canProceed: Boolean,
    isLoading: Boolean = false,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isFirstStep = currentStep == ProgressiveStep.entries.first()
    val isLastStep = currentStep == ProgressiveStep.entries.last()

    val buttonScale by animateFloatAsState(
        targetValue = if (canProceed) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "buttonScale",
    )

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = ShapeTokens.Corner.extraLarge,
            topEnd = ShapeTokens.Corner.extraLarge,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = ElevationTokens.large,
        ),
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.lg),
        ) {
            // Progress indicator
            if (!isLastStep) {
                StepProgressBar(
                    currentStep = currentStep,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(SpacingTokens.md))
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Previous button
                if (!isFirstStep) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPrevious()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(ShapeTokens.Corner.large),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(
                            imageVector = QodeActionIcons.Previous,
                            contentDescription = null,
                            modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        )
                        Spacer(modifier = Modifier.width(SpacingTokens.sm))
                        Text(
                            text = "Previous",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Next/Submit button
                PrimaryActionButton(
                    currentStep = currentStep,
                    canProceed = canProceed,
                    isLoading = isLoading,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNext()
                    },
                    modifier = Modifier
                        .weight(2f)
                        .scale(buttonScale),
                )
            }
        }
    }
}

@Composable
private fun StepProgressBar(
    currentStep: ProgressiveStep,
    modifier: Modifier = Modifier
) {
    val totalSteps = ProgressiveStep.entries.size
    val progress = currentStep.stepNumber.toFloat() / totalSteps

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = AnimationTokens.Spec.emphasized,
        label = "stepProgress",
    )

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Step ${currentStep.stepNumber} of $totalSteps",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )

            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(3.dp),
                    ),
            )
        }
    }
}

@Composable
private fun PrimaryActionButton(
    currentStep: ProgressiveStep,
    canProceed: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLastStep = currentStep == ProgressiveStep.entries.last()
    val buttonText = if (isLastStep) "Submit Promo Code" else "Continue"
    val buttonIcon = if (isLastStep) QodeActionIcons.Submit else QodeActionIcons.Next

    val buttonColors by animateColorAsState(
        targetValue = if (canProceed) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(),
        label = "buttonColor",
    )

    val contentColor by animateColorAsState(
        targetValue = if (canProceed) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(),
        label = "contentColor",
    )

    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        enabled = canProceed && !isLoading,
        modifier = modifier.height(SizeTokens.Button.heightLarge),
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColors,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        interactionSource = interactionSource,
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
            },
            label = "buttonContent",
        ) { loading ->
            if (loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        strokeWidth = 2.dp,
                        color = contentColor,
                    )
                    Text(
                        text = if (isLastStep) "Submitting..." else "Processing...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        imageVector = buttonIcon,
                        contentDescription = null,
                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingActionHint(
    message: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally { it } + fadeIn(),
        exit = slideOutHorizontally { it } + fadeOut(),
        modifier = modifier,
    ) {
        Card(
            shape = RoundedCornerShape(ShapeTokens.Corner.large),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f),
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = ElevationTokens.medium,
            ),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.padding(
                    horizontal = SpacingTokens.md,
                    vertical = SpacingTokens.sm,
                ),
            )
        }
    }
}

@Preview(name = "Modern Floating Controller - Middle Step", showBackground = true)
@Composable
private fun ModernFloatingControllerPreview() {
    QodeTheme {
        ModernFloatingController(
            currentStep = ProgressiveStep.DISCOUNT_VALUE,
            canProceed = true,
            onNext = {},
            onPrevious = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}

@Preview(name = "Modern Floating Controller - First Step", showBackground = true)
@Composable
private fun ModernFloatingControllerFirstStepPreview() {
    QodeTheme {
        ModernFloatingController(
            currentStep = ProgressiveStep.SERVICE,
            canProceed = false,
            onNext = {},
            onPrevious = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}

@Preview(name = "Modern Floating Controller - Last Step", showBackground = true)
@Composable
private fun ModernFloatingControllerLastStepPreview() {
    QodeTheme {
        ModernFloatingController(
            currentStep = ProgressiveStep.END_DATE,
            canProceed = true,
            isLoading = true,
            onNext = {},
            onPrevious = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
