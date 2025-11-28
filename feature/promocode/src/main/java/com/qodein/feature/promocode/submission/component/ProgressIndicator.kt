package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromocodeSubmissionStep
import com.qodein.feature.promocode.submission.shortNameRes
import com.qodein.feature.promocode.submission.stepIcon
import com.qodein.feature.promocode.submission.titleRes

@Composable
fun ProgressIndicator(
    currentStep: PromocodeSubmissionStep,
    modifier: Modifier = Modifier,
    totalSteps: Int = PromocodeSubmissionStep.entries.size,
    onStepClick: ((PromocodeSubmissionStep) -> Unit)? = null
) {
    val progress = (currentStep.stepNumber + 1f) / totalSteps

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeTokens.Corner.small),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = ElevationTokens.small,
        ),
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.lg),
        ) {
            // Step indicators with connections
            StepIndicatorRow(
                currentStep = currentStep,
                onStepClick = onStepClick,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            // Animated progress bar
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = AnimationTokens.Spec.emphasized(),
                label = "progress",
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SpacingTokens.xs)
                    .clip(RoundedCornerShape(SpacingTokens.xs / 2)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun StepIndicatorRow(
    currentStep: PromocodeSubmissionStep,
    onStepClick: ((PromocodeSubmissionStep) -> Unit)?,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        contentPadding = PaddingValues(horizontal = SpacingTokens.md),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        itemsIndexed(PromocodeSubmissionStep.entries) { index, step ->
            val isCompleted = step.stepNumber < currentStep.stepNumber
            val isCurrent = step.stepNumber == currentStep.stepNumber
            val isClickable = onStepClick != null && (isCompleted || isCurrent)

            // Step with icon and label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Step icon
                StepIcon(
                    step = step,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    isClickable = isClickable,
                    onClick = if (isClickable) {
                        { onStepClick(step) }
                    } else {
                        null
                    },
                )

                // Step label
                StepLabel(
                    step = step,
                    isCurrent = isCurrent,
                    isCompleted = isCompleted,
                )
            }
        }
    }
}

@Composable
private fun StepIcon(
    step: PromocodeSubmissionStep,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isClickable: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    // Visual distinction between required and optional steps
    val isOptional = !step.isRequired

    val containerColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary
        isCurrent -> if (isOptional) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
        else -> if (isOptional) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    }

    val contentColor = when {
        isCompleted -> MaterialTheme.colorScheme.onPrimary
        isCurrent -> if (isOptional) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }
        else -> if (isOptional) {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    val iconSize by animateFloatAsState(
        targetValue = if (isCurrent) SizeTokens.Icon.sizeLarge.value else SizeTokens.Icon.sizeMedium.value,
        animationSpec = AnimationTokens.Spec.emphasized(),
        label = "iconSize",
    )

    Box(
        modifier = modifier
            .size(SizeTokens.Icon.sizeXLarge)
            .background(
                color = containerColor,
                shape = CircleShape,
            )
            .clickable(
                enabled = isClickable,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onClick?.invoke() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = step.stepIcon(isCompleted),
            contentDescription = stringResource(step.titleRes),
            tint = contentColor,
            modifier = Modifier.size(iconSize.dp),
        )
    }
}

@Composable
private fun StepLabel(
    step: PromocodeSubmissionStep,
    isCurrent: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val isOptional = !step.isRequired

    Text(
        text = stringResource(step.shortNameRes),
        style = MaterialTheme.typography.labelSmall,
        color = when {
            isCurrent || isCompleted -> if (isOptional && isCurrent) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            else -> if (isOptional) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        },
        textAlign = TextAlign.Center,
        fontWeight = if (isCurrent) {
            FontWeight.SemiBold
        } else if (isOptional) {
            FontWeight.Light
        } else {
            FontWeight.Normal
        },
        modifier = modifier.padding(top = SpacingTokens.xs),
    )
}

@ThemePreviews
@Composable
private fun ProgressIndicatorPreview() {
    QodeTheme {
        ProgressIndicator(
            currentStep = PromocodeSubmissionStep.DISCOUNT_VALUE,
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
