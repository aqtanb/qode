package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.ProgressiveStep

@Composable
fun EnhancedProgressIndicator(
    currentStep: ProgressiveStep,
    totalSteps: Int = ProgressiveStep.entries.size,
    modifier: Modifier = Modifier
) {
    val progress = currentStep.stepNumber.toFloat() / totalSteps

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
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
            // Header with current step info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = currentStep.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "${currentStep.stepNumber}/$totalSteps",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            // Step indicators with connections
            StepIndicatorRow(
                currentStep = currentStep,
                totalSteps = totalSteps,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            // Animated progress bar
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = AnimationTokens.Spec.emphasized,
                label = "progress",
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun StepIndicatorRow(
    currentStep: ProgressiveStep,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = SpacingTokens.xs),
    ) {
        itemsIndexed(ProgressiveStep.entries) { index, step ->
            val isCompleted = step.stepNumber < currentStep.stepNumber
            val isCurrent = step.stepNumber == currentStep.stepNumber
            val isPending = step.stepNumber > currentStep.stepNumber

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Step indicator
                StepIndicator(
                    step = step,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    modifier = Modifier,
                )

                // Connection line (except for last step)
                if (index < totalSteps - 1) {
                    Spacer(modifier = Modifier.width(SpacingTokens.xs))
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    step: ProgressiveStep,
    isCompleted: Boolean,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isCurrent) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale",
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.primary
            isCurrent -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(),
        label = "containerColor",
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.onPrimary
            isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(),
        label = "contentColor",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(SizeTokens.Icon.sizeXLarge)
                .scale(scale)
                .background(
                    color = containerColor,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = getStepIcon(step, isCompleted),
                contentDescription = step.displayName,
                tint = contentColor,
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.xs))

        Text(
            text = step.shortName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isCurrent || isCompleted) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ConnectionLine(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(),
        label = "connectionColor",
    )

    Canvas(
        modifier = modifier
            .height(2.dp)
            .padding(horizontal = SpacingTokens.xs),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private fun getStepIcon(
    step: ProgressiveStep,
    isCompleted: Boolean
): ImageVector =
    if (isCompleted) {
        QodeActionIcons.Check
    } else {
        when (step) {
            ProgressiveStep.SERVICE -> QodeCommerceIcons.Store
            ProgressiveStep.DISCOUNT_TYPE -> QodeCommerceIcons.Sale
            ProgressiveStep.PROMO_CODE -> QodeCommerceIcons.PromoCode
            ProgressiveStep.DISCOUNT_VALUE -> QodeCommerceIcons.Dollar
            ProgressiveStep.OPTIONS -> QodeNavigationIcons.Settings
            ProgressiveStep.START_DATE, ProgressiveStep.END_DATE -> QodeUIIcons.Datepicker
        }
    }

private val ProgressiveStep.displayName: String
    get() = when (this) {
        ProgressiveStep.SERVICE -> "Select Service"
        ProgressiveStep.DISCOUNT_TYPE -> "Discount Type"
        ProgressiveStep.PROMO_CODE -> "Promo Code"
        ProgressiveStep.DISCOUNT_VALUE -> "Discount Value"
        ProgressiveStep.OPTIONS -> "Options"
        ProgressiveStep.START_DATE -> "Start Date"
        ProgressiveStep.END_DATE -> "End Date"
    }

private val ProgressiveStep.shortName: String
    get() = when (this) {
        ProgressiveStep.SERVICE -> "Service"
        ProgressiveStep.DISCOUNT_TYPE -> "Type"
        ProgressiveStep.PROMO_CODE -> "Code"
        ProgressiveStep.DISCOUNT_VALUE -> "Value"
        ProgressiveStep.OPTIONS -> "Options"
        ProgressiveStep.START_DATE -> "Start"
        ProgressiveStep.END_DATE -> "End"
    }

@Preview(name = "Enhanced Progress Indicator - Current Step", showBackground = true)
@Composable
private fun EnhancedProgressIndicatorPreview() {
    QodeTheme {
        EnhancedProgressIndicator(
            currentStep = ProgressiveStep.DISCOUNT_VALUE,
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}

@Preview(name = "Enhanced Progress Indicator - First Step", showBackground = true)
@Composable
private fun EnhancedProgressIndicatorFirstStepPreview() {
    QodeTheme {
        EnhancedProgressIndicator(
            currentStep = ProgressiveStep.SERVICE,
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
