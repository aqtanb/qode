package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.ProgressiveStep
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.ServiceSelectionUiState
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData

@Composable
fun FloatingStepCard(
    currentStep: ProgressiveStep,
    wizardData: SubmissionWizardData,
    serviceSelectionUiState: ServiceSelectionUiState,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "cardScale",
    )

    // Dynamic container color based on step progress
    val containerColor by animateColorAsState(
        targetValue = getStepContainerColor(currentStep, wizardData),
        animationSpec = spring(dampingRatio = 0.8f),
        label = "containerColor",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .animateContentSize(),
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = ElevationTokens.small,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.xl),
        ) {
            // Enhanced step header with progress indicator
            EnhancedStepHeader(
                step = currentStep,
                wizardData = wizardData,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.xl))

            // Step instruction card
            StepInstructionCard(
                step = currentStep,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.lg))

            // Animated step content with enhanced transitions
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (
                        slideInHorizontally { width -> width / 2 } + fadeIn(
                            animationSpec = tween(300, delayMillis = 150),
                        )
                        ).togetherWith(
                        slideOutHorizontally { width -> -width / 2 } + fadeOut(
                            animationSpec = tween(150),
                        ),
                    )
                },
                label = "stepContent",
            ) { step ->
                CurrentStepContent(
                    currentStep = step,
                    wizardData = wizardData,
                    serviceSelectionUiState = serviceSelectionUiState,
                    onAction = onAction,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun EnhancedStepHeader(
    step: ProgressiveStep,
    wizardData: SubmissionWizardData,
    modifier: Modifier = Modifier
) {
    val isStepCompleted = isStepCompleted(step, wizardData)
    val iconScale by animateFloatAsState(
        targetValue = if (isStepCompleted) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "iconScale",
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Enhanced step icon with completion state
            Box(
                modifier = Modifier
                    .size(SizeTokens.Icon.sizeXLarge + 12.dp)
                    .scale(iconScale)
                    .background(
                        color = if (isStepCompleted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        shape = RoundedCornerShape(ShapeTokens.Corner.large),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isStepCompleted) {
                        QodeActionIcons.Check
                    } else {
                        getStepIcon(step)
                    },
                    contentDescription = null,
                    tint = if (isStepCompleted) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = step.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Step completion indicator
            if (isStepCompleted) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(ShapeTokens.Corner.small),
                        )
                        .padding(
                            horizontal = SpacingTokens.sm,
                            vertical = SpacingTokens.xs,
                        ),
                ) {
                    Text(
                        text = "Complete",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        // Progress indicator for current step
        StepProgressIndicator(
            step = step,
            wizardData = wizardData,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun getStepIcon(step: ProgressiveStep): ImageVector =
    when (step) {
        ProgressiveStep.SERVICE -> QodeCommerceIcons.Store
        ProgressiveStep.DISCOUNT_TYPE -> QodeCommerceIcons.Sale
        ProgressiveStep.PROMO_CODE -> QodeCommerceIcons.PromoCode
        ProgressiveStep.DISCOUNT_VALUE -> QodeCommerceIcons.Dollar
        ProgressiveStep.OPTIONAL -> QodeNavigationIcons.Settings
        ProgressiveStep.START_DATE, ProgressiveStep.END_DATE -> QodeUIIcons.Datepicker
    }

private val ProgressiveStep.title: String
    get() = when (this) {
        ProgressiveStep.SERVICE -> "Choose Service"
        ProgressiveStep.DISCOUNT_TYPE -> "Discount Type"
        ProgressiveStep.PROMO_CODE -> "Promo Code"
        ProgressiveStep.DISCOUNT_VALUE -> "Set Value"
        ProgressiveStep.OPTIONAL -> "Additional Options"
        ProgressiveStep.START_DATE -> "Start Date"
        ProgressiveStep.END_DATE -> "End Date"
    }

private val ProgressiveStep.subtitle: String
    get() = when (this) {
        ProgressiveStep.SERVICE -> "Select the service for your promo code"
        ProgressiveStep.DISCOUNT_TYPE -> "Choose percentage or fixed amount"
        ProgressiveStep.PROMO_CODE -> "Enter your promotional code"
        ProgressiveStep.DISCOUNT_VALUE -> "Set discount amount and minimum order"
        ProgressiveStep.OPTIONAL -> "Configure who can use this code"
        ProgressiveStep.START_DATE -> "When should this promo code start?"
        ProgressiveStep.END_DATE -> "When should this promo code expire?"
    }

@Composable
fun StepWithInstructions(
    step: ProgressiveStep,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        // Instructions card
        Card(
            shape = RoundedCornerShape(ShapeTokens.Corner.large),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                Icon(
                    imageVector = QodeUIIcons.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )

                Text(
                    text = getStepInstruction(step),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Main content
        content()
    }
}

private fun getStepInstruction(step: ProgressiveStep): String =
    when (step) {
        ProgressiveStep.SERVICE -> "Search for a service or enter manually"
        ProgressiveStep.DISCOUNT_TYPE -> "Choose how you want to discount"
        ProgressiveStep.PROMO_CODE -> "Make it memorable and unique"
        ProgressiveStep.DISCOUNT_VALUE -> "Set attractive but sustainable values"
        ProgressiveStep.OPTIONAL -> "Decide who can use this code"
        ProgressiveStep.START_DATE -> "Choose when customers can start using it"
        ProgressiveStep.END_DATE -> "Set when the code should expire"
    }

@Composable
private fun StepInstructionCard(
    step: ProgressiveStep,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Icon(
                imageVector = QodeUIIcons.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
            )

            Text(
                text = getStepInstruction(step),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StepProgressIndicator(
    step: ProgressiveStep,
    wizardData: SubmissionWizardData,
    modifier: Modifier = Modifier
) {
    val progress = getStepProgress(step, wizardData)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "stepProgress",
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Step Progress",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        androidx.compose.material3.LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        )
    }
}

private fun isStepCompleted(
    step: ProgressiveStep,
    wizardData: SubmissionWizardData
): Boolean =
    when (step) {
        ProgressiveStep.SERVICE -> wizardData.selectedService != null || wizardData.serviceName.isNotEmpty()
        ProgressiveStep.DISCOUNT_TYPE -> wizardData.promoCodeType != null
        ProgressiveStep.PROMO_CODE -> wizardData.promoCode.isNotEmpty() && wizardData.promoCode.length >= 3
        ProgressiveStep.DISCOUNT_VALUE -> when (wizardData.promoCodeType) {
            PromoCodeType.PERCENTAGE -> wizardData.discountPercentage.isNotEmpty()
            PromoCodeType.FIXED_AMOUNT -> wizardData.discountAmount.isNotEmpty()
            null -> false
        }
        ProgressiveStep.OPTIONAL -> true // Always considered complete as it has defaults
        ProgressiveStep.START_DATE -> true // Has default value
        ProgressiveStep.END_DATE -> true // Optional field
    }

private fun getStepProgress(
    step: ProgressiveStep,
    wizardData: SubmissionWizardData
): Float =
    when (step) {
        ProgressiveStep.SERVICE -> {
            val hasService = wizardData.selectedService != null
            val hasServiceName = wizardData.serviceName.isNotEmpty()
            when {
                hasService && hasServiceName -> 1.0f
                hasService || hasServiceName -> 0.7f
                else -> 0.0f
            }
        }
        ProgressiveStep.DISCOUNT_TYPE -> if (wizardData.promoCodeType != null) 1.0f else 0.0f
        ProgressiveStep.PROMO_CODE -> when {
            wizardData.promoCode.isEmpty() -> 0.0f
            wizardData.promoCode.length < 3 -> 0.3f
            wizardData.promoCode.length < 6 -> 0.7f
            else -> 1.0f
        }
        ProgressiveStep.DISCOUNT_VALUE -> when (wizardData.promoCodeType) {
            PromoCodeType.PERCENTAGE -> {
                val percentage = wizardData.discountPercentage.toIntOrNull() ?: 0
                val hasMinOrder = wizardData.minimumOrderAmount.isNotEmpty()
                when {
                    percentage > 0 && hasMinOrder -> 1.0f
                    percentage > 0 -> 0.7f
                    hasMinOrder -> 0.3f
                    else -> 0.0f
                }
            }
            PromoCodeType.FIXED_AMOUNT -> {
                val amount = wizardData.discountAmount.toIntOrNull() ?: 0
                val hasMinOrder = wizardData.minimumOrderAmount.isNotEmpty()
                when {
                    amount > 0 && hasMinOrder -> 1.0f
                    amount > 0 -> 0.7f
                    hasMinOrder -> 0.3f
                    else -> 0.0f
                }
            }
            null -> 0.0f
        }
        ProgressiveStep.OPTIONAL -> {
            val hasDescription = wizardData.description.isNotEmpty()
            if (hasDescription) 1.0f else 0.7f
        }
        ProgressiveStep.START_DATE -> 1.0f
        ProgressiveStep.END_DATE -> 1.0f
    }

@Composable
private fun getStepContainerColor(
    step: ProgressiveStep,
    wizardData: SubmissionWizardData
): androidx.compose.ui.graphics.Color {
    val isCompleted = isStepCompleted(step, wizardData)
    val progress = getStepProgress(step, wizardData)

    return when {
        isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        progress > 0.5f -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        progress > 0.0f -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surface
    }
}

@Preview(name = "Floating Step Card - Service", showBackground = true)
@Composable
private fun FloatingStepCardServicePreview() {
    QodeTheme {
        FloatingStepCard(
            currentStep = ProgressiveStep.SERVICE,
            wizardData = SubmissionWizardData(),
            serviceSelectionUiState = ServiceSelectionUiState.Default,
            onAction = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}

@Preview(name = "Floating Step Card - Promo Code", showBackground = true)
@Composable
private fun FloatingStepCardPromoCodePreview() {
    QodeTheme {
        FloatingStepCard(
            currentStep = ProgressiveStep.PROMO_CODE,
            wizardData = SubmissionWizardData(),
            serviceSelectionUiState = ServiceSelectionUiState.Default,
            onAction = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
