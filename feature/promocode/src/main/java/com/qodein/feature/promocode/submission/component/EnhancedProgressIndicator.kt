package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.feature.promocode.submission.ProgressiveStep
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData

enum class StepValidationState {
    NONE, // No validation applied
    VALID, // Step is complete and valid
    INCOMPLETE, // Step has some data but is not complete
    ERROR, // Step has validation errors
    WARNING // Step has warnings
}

@Composable
fun EnhancedProgressIndicator(
    currentStep: ProgressiveStep,
    wizardData: SubmissionWizardData? = null,
    totalSteps: Int = ProgressiveStep.entries.size,
    onStepClick: ((ProgressiveStep) -> Unit)? = null,
    showValidationStatus: Boolean = true,
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

            // Step indicators with connections and validation status
            StepIndicatorRow(
                currentStep = currentStep,
                wizardData = wizardData,
                totalSteps = totalSteps,
                onStepClick = onStepClick,
                showValidationStatus = showValidationStatus,
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
    wizardData: SubmissionWizardData?,
    totalSteps: Int,
    onStepClick: ((ProgressiveStep) -> Unit)?,
    showValidationStatus: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
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

            // Enhanced validation status
            val stepValidationState = if (showValidationStatus && wizardData != null) {
                getStepValidationState(step, wizardData)
            } else {
                StepValidationState.NONE
            }

            val isClickable = onStepClick != null && (isCompleted || isCurrent)

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Enhanced step indicator with validation and interaction
                EnhancedStepIndicator(
                    step = step,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    isPending = isPending,
                    validationState = stepValidationState,
                    isClickable = isClickable,
                    onClick = if (isClickable) {
                        {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStepClick?.invoke(step)
                        }
                    } else {
                        null
                    },
                    modifier = Modifier,
                )

                // Enhanced connection line with validation status
                if (index < totalSteps - 1) {
                    val connectionActive = isCompleted ||
                        (isCurrent && stepValidationState == StepValidationState.VALID)

                    EnhancedConnectionLine(
                        isActive = connectionActive,
                        validationState = stepValidationState,
                        modifier = Modifier.width(SpacingTokens.sm),
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedStepIndicator(
    step: ProgressiveStep,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isPending: Boolean,
    validationState: StepValidationState,
    isClickable: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isCurrent) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale",
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.primary
            isCurrent -> when (validationState) {
                StepValidationState.VALID -> MaterialTheme.colorScheme.primaryContainer
                StepValidationState.ERROR -> MaterialTheme.colorScheme.errorContainer
                StepValidationState.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                StepValidationState.INCOMPLETE -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.primaryContainer
            }
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(),
        label = "containerColor",
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.onPrimary
            isCurrent -> when (validationState) {
                StepValidationState.VALID -> MaterialTheme.colorScheme.onPrimaryContainer
                StepValidationState.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                StepValidationState.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                StepValidationState.INCOMPLETE -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onPrimaryContainer
            }
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(),
        label = "contentColor",
    )

    val borderColor by animateColorAsState(
        targetValue = when (validationState) {
            StepValidationState.ERROR -> MaterialTheme.colorScheme.error
            StepValidationState.WARNING -> MaterialTheme.colorScheme.tertiary
            StepValidationState.VALID -> MaterialTheme.colorScheme.primary
            else -> androidx.compose.ui.graphics.Color.Transparent
        },
        animationSpec = spring(),
        label = "borderColor",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .let {
                if (isClickable && onClick != null) {
                    it.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) { onClick() }
                } else {
                    it
                }
            },
    ) {
        Box(
            modifier = Modifier
                .size(SizeTokens.Icon.sizeXLarge)
                .scale(scale)
                .background(
                    color = containerColor,
                    shape = CircleShape,
                )
                .let {
                    if (borderColor != androidx.compose.ui.graphics.Color.Transparent) {
                        it.background(
                            borderColor.copy(alpha = 0.3f),
                            CircleShape,
                        )
                    } else {
                        it
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = getEnhancedStepIcon(step, isCompleted, validationState),
                contentDescription = step.displayName,
                tint = contentColor,
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.xs))

        Text(
            text = step.shortName,
            style = MaterialTheme.typography.labelSmall,
            color = when {
                isCurrent || isCompleted -> MaterialTheme.colorScheme.onSurface
                validationState == StepValidationState.ERROR -> MaterialTheme.colorScheme.error
                validationState == StepValidationState.WARNING -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
        )

        // Validation indicator dot
        if (validationState != StepValidationState.NONE && !isCurrent) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = when (validationState) {
                            StepValidationState.VALID -> MaterialTheme.colorScheme.primary
                            StepValidationState.ERROR -> MaterialTheme.colorScheme.error
                            StepValidationState.WARNING -> MaterialTheme.colorScheme.tertiary
                            StepValidationState.INCOMPLETE -> MaterialTheme.colorScheme.surfaceVariant
                            else -> androidx.compose.ui.graphics.Color.Transparent
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun EnhancedConnectionLine(
    isActive: Boolean,
    validationState: StepValidationState,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = when {
            isActive && validationState == StepValidationState.VALID -> MaterialTheme.colorScheme.primary
            isActive && validationState == StepValidationState.ERROR -> MaterialTheme.colorScheme.error
            isActive && validationState == StepValidationState.WARNING -> MaterialTheme.colorScheme.tertiary
            isActive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(),
        label = "connectionColor",
    )

    val strokeWidth by animateFloatAsState(
        targetValue = if (isActive) 4.dp.value else 2.dp.value,
        animationSpec = spring(),
        label = "strokeWidth",
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
            strokeWidth = strokeWidth.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private fun getEnhancedStepIcon(
    step: ProgressiveStep,
    isCompleted: Boolean,
    validationState: StepValidationState
): ImageVector =
    when {
        isCompleted -> QodeActionIcons.Check
        validationState == StepValidationState.ERROR -> QodeUIIcons.Error
        validationState == StepValidationState.WARNING -> QodeUIIcons.Info
        else -> when (step) {
            ProgressiveStep.SERVICE -> QodeCommerceIcons.Store
            ProgressiveStep.DISCOUNT_TYPE -> QodeCommerceIcons.Sale
            ProgressiveStep.PROMO_CODE -> QodeCommerceIcons.PromoCode
            ProgressiveStep.DISCOUNT_VALUE -> QodeCommerceIcons.Dollar
            ProgressiveStep.OPTIONS -> QodeNavigationIcons.Settings
            ProgressiveStep.START_DATE, ProgressiveStep.END_DATE -> QodeUIIcons.Datepicker
        }
    }

// Enhanced validation logic for steps
private fun getStepValidationState(
    step: ProgressiveStep,
    wizardData: SubmissionWizardData
): StepValidationState =
    when (step) {
        ProgressiveStep.SERVICE -> {
            val hasService = wizardData.selectedService != null
            val hasServiceName = wizardData.serviceName.isNotEmpty()
            when {
                hasService && hasServiceName -> StepValidationState.VALID
                hasService || hasServiceName -> StepValidationState.INCOMPLETE
                else -> StepValidationState.NONE
            }
        }

        ProgressiveStep.DISCOUNT_TYPE -> {
            if (wizardData.promoCodeType != null) StepValidationState.VALID else StepValidationState.NONE
        }

        ProgressiveStep.PROMO_CODE -> {
            when {
                wizardData.promoCode.isEmpty() -> StepValidationState.NONE
                wizardData.promoCode.length < 3 -> StepValidationState.ERROR
                wizardData.promoCode.length < 6 -> StepValidationState.WARNING
                else -> StepValidationState.VALID
            }
        }

        ProgressiveStep.DISCOUNT_VALUE -> {
            when (wizardData.promoCodeType) {
                PromoCodeType.PERCENTAGE -> {
                    val percentage = wizardData.discountPercentage.toIntOrNull() ?: 0
                    when {
                        percentage <= 0 -> StepValidationState.NONE
                        percentage > 99 -> StepValidationState.ERROR
                        percentage > 75 -> StepValidationState.WARNING
                        else -> StepValidationState.VALID
                    }
                }
                PromoCodeType.FIXED_AMOUNT -> {
                    val amount = wizardData.discountAmount.toIntOrNull() ?: 0
                    when {
                        amount <= 0 -> StepValidationState.NONE
                        amount > 10000 -> StepValidationState.WARNING
                        else -> StepValidationState.VALID
                    }
                }
                null -> StepValidationState.NONE
            }
        }

        ProgressiveStep.OPTIONS -> {
            // Options always considered valid as they have defaults
            StepValidationState.VALID
        }

        ProgressiveStep.START_DATE -> StepValidationState.VALID
        ProgressiveStep.END_DATE -> StepValidationState.VALID
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

@Preview(name = "Enhanced Progress Indicator - With Validation", showBackground = true)
@Composable
private fun EnhancedProgressIndicatorWithValidationPreview() {
    QodeTheme {
        EnhancedProgressIndicator(
            currentStep = ProgressiveStep.PROMO_CODE,
            wizardData = SubmissionWizardData(
                selectedService = ServicePreviewData.localCoffeeShop,
                promoCodeType = PromoCodeType.PERCENTAGE,
                promoCode = "TEST",
                discountPercentage = "20",
            ),
            showValidationStatus = true,
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}

@Preview(name = "Enhanced Progress Indicator - Interactive", showBackground = true)
@Composable
private fun EnhancedProgressIndicatorInteractivePreview() {
    QodeTheme {
        EnhancedProgressIndicator(
            currentStep = ProgressiveStep.DISCOUNT_VALUE,
            onStepClick = { /* Handle step click */ },
            showValidationStatus = true,
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
