package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.QodeinIconButton
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep
import com.qodein.feature.promocode.submission.wizard.shortNameRes
import com.qodein.feature.promocode.submission.wizard.stepIcon
import com.qodein.feature.promocode.submission.wizard.titleRes

@Composable
fun ProgressIndicator(
    currentStep: PromocodeSubmissionStep,
    modifier: Modifier = Modifier,
    totalSteps: Int = PromocodeSubmissionStep.entries.size,
    onStepClick: ((PromocodeSubmissionStep) -> Unit)? = null
) {
    val progress = (currentStep.stepNumber.toFloat()) / totalSteps

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
                    .height(6.dp)
                    .clip(RoundedCornerShape(ShapeTokens.Corner.full)),
                color = MaterialTheme.colorScheme.secondaryContainer,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
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
        contentPadding = PaddingValues(horizontal = SpacingTokens.sm),
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
        isCompleted -> MaterialTheme.colorScheme.secondaryContainer
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
        isCompleted -> MaterialTheme.colorScheme.onSecondaryContainer
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

    QodeinIconButton(
        onClick = { onClick?.invoke() },
        icon = step.stepIcon(isCompleted),
        contentDescription = stringResource(step.titleRes),
        size = ButtonSize.Small,
        enabled = isClickable,
        containerColor = containerColor,
        contentColor = contentColor,
    )
}

@Composable
private fun StepLabel(
    step: PromocodeSubmissionStep,
    isCurrent: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(step.shortNameRes),
        style = MaterialTheme.typography.labelSmall,
        color = when {
            isCurrent -> MaterialTheme.colorScheme.secondary
            isCompleted -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        textAlign = TextAlign.Center,
        fontWeight = if (isCurrent) {
            FontWeight.SemiBold
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
            onStepClick = {},
        )
    }
}
