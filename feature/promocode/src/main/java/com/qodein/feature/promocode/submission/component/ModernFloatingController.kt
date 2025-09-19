package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.ProgressiveStep
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData

data class StepValidationResult(
    val isValid: Boolean,
    val canProceed: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val completionPercentage: Float = 0f,
    val nextAction: String = "",
    val helpText: String? = null
)

data class ControllerState(
    val currentStep: ProgressiveStep,
    val wizardData: SubmissionWizardData,
    val validationResult: StepValidationResult,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = true,
    val showHints: Boolean = true
)

@Composable
fun ModernFloatingController(
    currentStep: ProgressiveStep,
    wizardData: SubmissionWizardData,
    isLoading: Boolean = false,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onQuickAction: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isFirstStep = currentStep == ProgressiveStep.entries.first()
    val isLastStep = currentStep == ProgressiveStep.entries.last()

    // Smart validation and context awareness
    val validationResult = remember(currentStep, wizardData) {
        validateCurrentStep(currentStep, wizardData)
    }

    val controllerState = ControllerState(
        currentStep = currentStep,
        wizardData = wizardData,
        validationResult = validationResult,
        isLoading = isLoading,
        canGoBack = !isFirstStep,
        showHints = !validationResult.isValid,
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (validationResult.canProceed) 1f else 0.95f,
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
            // Context-aware help and validation feedback
            SmartValidationFeedback(
                controllerState = controllerState,
                onQuickAction = onQuickAction,
                modifier = Modifier.fillMaxWidth(),
            )

            // Enhanced progress indicator with validation status
            if (!isLastStep) {
                Spacer(modifier = Modifier.height(SpacingTokens.md))
                EnhancedStepProgressBar(
                    controllerState = controllerState,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(SpacingTokens.md))
            }

            // Smart navigation with context-aware actions
            SmartNavigationRow(
                controllerState = controllerState,
                onNext = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNext()
                },
                onPrevious = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPrevious()
                },
                onQuickAction = onQuickAction,
                buttonScale = buttonScale,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SmartValidationFeedback(
    controllerState: ControllerState,
    onQuickAction: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val validationResult = controllerState.validationResult

    AnimatedVisibility(
        visible = validationResult.errors.isNotEmpty() || validationResult.warnings.isNotEmpty() || validationResult.helpText != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            // Error messages
            validationResult.errors.forEach { error ->
                ValidationMessage(
                    message = error,
                    type = ValidationMessageType.ERROR,
                    onQuickAction = onQuickAction,
                )
            }

            // Warning messages
            validationResult.warnings.forEach { warning ->
                ValidationMessage(
                    message = warning,
                    type = ValidationMessageType.WARNING,
                    onQuickAction = onQuickAction,
                )
            }

            // Help text
            validationResult.helpText?.let { help ->
                ValidationMessage(
                    message = help,
                    type = ValidationMessageType.INFO,
                    onQuickAction = onQuickAction,
                )
            }
        }
    }
}

@Composable
private fun ValidationMessage(
    message: String,
    type: ValidationMessageType,
    onQuickAction: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeTokens.Corner.medium),
        colors = CardDefaults.cardColors(
            containerColor = when (type) {
                ValidationMessageType.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ValidationMessageType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                ValidationMessageType.INFO -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Icon(
                imageVector = when (type) {
                    ValidationMessageType.ERROR -> QodeUIIcons.Error
                    ValidationMessageType.WARNING -> QodeUIIcons.Info
                    ValidationMessageType.INFO -> QodeUIIcons.Info
                },
                contentDescription = null,
                tint = when (type) {
                    ValidationMessageType.ERROR -> MaterialTheme.colorScheme.error
                    ValidationMessageType.WARNING -> MaterialTheme.colorScheme.tertiary
                    ValidationMessageType.INFO -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            // Quick action button if available
            if (onQuickAction != null && message.contains("Quick:")) {
                val quickActionText = message.substringAfter("Quick:").trim()
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(ShapeTokens.Corner.small),
                        )
                        .clickable { onQuickAction(quickActionText) }
                        .padding(
                            horizontal = SpacingTokens.sm,
                            vertical = SpacingTokens.xs,
                        ),
                ) {
                    Text(
                        text = "Fix",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

enum class ValidationMessageType {
    ERROR,
    WARNING,
    INFO
}

@Composable
private fun EnhancedStepProgressBar(
    controllerState: ControllerState,
    modifier: Modifier = Modifier
) {
    val currentStep = controllerState.currentStep
    val validationResult = controllerState.validationResult
    val totalSteps = ProgressiveStep.entries.size
    val baseProgress = currentStep.stepNumber.toFloat() / totalSteps
    val adjustedProgress = baseProgress + (validationResult.completionPercentage / totalSteps)

    val animatedProgress by animateFloatAsState(
        targetValue = adjustedProgress.coerceIn(0f, 1f),
        animationSpec = AnimationTokens.Spec.emphasized,
        label = "stepProgress",
    )

    val progressColor by animateColorAsState(
        targetValue = when {
            validationResult.errors.isNotEmpty() -> MaterialTheme.colorScheme.error
            validationResult.warnings.isNotEmpty() -> MaterialTheme.colorScheme.tertiary
            validationResult.isValid -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(),
        label = "progressColor",
    )

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Step ${currentStep.stepNumber} of $totalSteps",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )

                // Step completion status
                Text(
                    text = when {
                        validationResult.isValid -> "Complete"
                        validationResult.completionPercentage > 0f -> "In Progress"
                        else -> "Pending"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        validationResult.isValid -> MaterialTheme.colorScheme.primary
                        validationResult.completionPercentage > 0f -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Medium,
                )
            }

            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = progressColor,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        // Enhanced progress bar with validation indication
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
                        color = progressColor,
                        shape = RoundedCornerShape(3.dp),
                    ),
            )
        }
    }
}

@Composable
private fun SmartNavigationRow(
    controllerState: ControllerState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onQuickAction: ((String) -> Unit)?,
    buttonScale: Float,
    modifier: Modifier = Modifier
) {
    val validationResult = controllerState.validationResult
    val isFirstStep = !controllerState.canGoBack
    val isLastStep = controllerState.currentStep == ProgressiveStep.entries.last()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Previous button with smart visibility
        if (!isFirstStep) {
            OutlinedButton(
                onClick = onPrevious,
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

        // Enhanced primary action button
        EnhancedPrimaryActionButton(
            controllerState = controllerState,
            onClick = onNext,
            modifier = Modifier
                .weight(2f)
                .scale(buttonScale),
        )
    }
}

@Composable
private fun EnhancedPrimaryActionButton(
    controllerState: ControllerState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStep = controllerState.currentStep
    val validationResult = controllerState.validationResult
    val isLoading = controllerState.isLoading
    val isLastStep = currentStep == ProgressiveStep.entries.last()

    val buttonText = when {
        isLoading -> if (isLastStep) "Submitting..." else "Processing..."
        !validationResult.canProceed -> "Complete Required Fields"
        isLastStep -> "Submit Promo Code"
        else -> validationResult.nextAction.ifEmpty { "Continue" }
    }

    val buttonIcon = when {
        isLoading -> null
        !validationResult.canProceed -> QodeUIIcons.Info
        isLastStep -> QodeActionIcons.Submit
        else -> QodeActionIcons.Next
    }

    val buttonColors by animateColorAsState(
        targetValue = when {
            !validationResult.canProceed -> MaterialTheme.colorScheme.surfaceVariant
            validationResult.errors.isNotEmpty() -> MaterialTheme.colorScheme.errorContainer
            validationResult.warnings.isNotEmpty() -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = spring(),
        label = "buttonColor",
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !validationResult.canProceed -> MaterialTheme.colorScheme.onSurfaceVariant
            validationResult.errors.isNotEmpty() -> MaterialTheme.colorScheme.onErrorContainer
            validationResult.warnings.isNotEmpty() -> MaterialTheme.colorScheme.onTertiaryContainer
            else -> MaterialTheme.colorScheme.onPrimary
        },
        animationSpec = spring(),
        label = "contentColor",
    )

    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        enabled = validationResult.canProceed && !isLoading,
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
                        text = buttonText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
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
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    buttonIcon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        )
                    }
                }
            }
        }
    }
}

// Smart validation logic for each step
private fun validateCurrentStep(
    currentStep: ProgressiveStep,
    wizardData: SubmissionWizardData
): StepValidationResult =
    when (currentStep) {
        ProgressiveStep.SERVICE -> validateServiceStep(wizardData)
        ProgressiveStep.DISCOUNT_TYPE -> validateDiscountTypeStep(wizardData)
        ProgressiveStep.PROMO_CODE -> validatePromoCodeStep(wizardData)
        ProgressiveStep.DISCOUNT_VALUE -> validateDiscountValueStep(wizardData)
        ProgressiveStep.OPTIONS -> validateOptionsStep(wizardData)
        ProgressiveStep.START_DATE -> validateStartDateStep(wizardData)
        ProgressiveStep.END_DATE -> validateEndDateStep(wizardData)
    }

private fun validateServiceStep(wizardData: SubmissionWizardData): StepValidationResult {
    val hasService = wizardData.selectedService != null
    val hasServiceName = wizardData.serviceName.isNotEmpty()

    return when {
        hasService && hasServiceName -> StepValidationResult(
            isValid = true,
            canProceed = true,
            completionPercentage = 1.0f,
            nextAction = "Set Discount Type",
            helpText = "Perfect! You've selected a service.",
        )
        hasService || hasServiceName -> StepValidationResult(
            isValid = false,
            canProceed = true,
            completionPercentage = 0.6f,
            nextAction = "Continue Anyway",
            warnings = listOf("You can add more service details or continue with what you have."),
        )
        else -> StepValidationResult(
            isValid = false,
            canProceed = false,
            completionPercentage = 0.0f,
            errors = listOf("Please select a service or enter a service name."),
            helpText = "Choose from popular services or enter a custom service name.",
        )
    }
}

private fun validateDiscountTypeStep(wizardData: SubmissionWizardData): StepValidationResult =
    if (wizardData.promoCodeType != null) {
        StepValidationResult(
            isValid = true,
            canProceed = true,
            completionPercentage = 1.0f,
            nextAction = "Create Promo Code",
            helpText = "Great choice! Now let's create your promo code.",
        )
    } else {
        StepValidationResult(
            isValid = false,
            canProceed = false,
            completionPercentage = 0.0f,
            errors = listOf("Please select a discount type."),
            helpText = "Choose between percentage discount or fixed amount.",
        )
    }

private fun validatePromoCodeStep(wizardData: SubmissionWizardData): StepValidationResult {
    val promoCode = wizardData.promoCode

    return when {
        promoCode.isEmpty() -> StepValidationResult(
            isValid = false,
            canProceed = false,
            completionPercentage = 0.0f,
            errors = listOf("Please enter a promo code."),
            helpText = "Create a memorable and unique promo code (3-20 characters).",
        )
        promoCode.length < 3 -> StepValidationResult(
            isValid = false,
            canProceed = false,
            completionPercentage = 0.3f,
            errors = listOf("Promo code must be at least 3 characters long."),
            helpText = "Make it longer for better uniqueness. Quick: ${promoCode}SAVE",
        )
        promoCode.length < 6 -> StepValidationResult(
            isValid = true,
            canProceed = true,
            completionPercentage = 0.7f,
            nextAction = "Set Discount Value",
            warnings = listOf("Consider making it longer for better memorability. Quick: ${promoCode}20"),
        )
        else -> StepValidationResult(
            isValid = true,
            canProceed = true,
            completionPercentage = 1.0f,
            nextAction = "Set Discount Value",
            helpText = "Perfect code! Clear and memorable.",
        )
    }
}

private fun validateDiscountValueStep(wizardData: SubmissionWizardData): StepValidationResult =
    when (wizardData.promoCodeType) {
        PromoCodeType.PERCENTAGE -> {
            val percentage = wizardData.discountPercentage.toIntOrNull() ?: 0
            when {
                percentage <= 0 -> StepValidationResult(
                    isValid = false,
                    canProceed = false,
                    completionPercentage = 0.0f,
                    errors = listOf("Please enter a discount percentage."),
                    helpText = "Enter a percentage between 1% and 99%.",
                )
                percentage > 99 -> StepValidationResult(
                    isValid = false,
                    canProceed = false,
                    completionPercentage = 0.3f,
                    errors = listOf("Percentage cannot exceed 99%."),
                    helpText = "Keep it reasonable for your business. Quick: 20",
                )
                percentage > 75 -> StepValidationResult(
                    isValid = true,
                    canProceed = true,
                    completionPercentage = 0.8f,
                    nextAction = "Add Options",
                    warnings = listOf("High discount - make sure this works for your margins."),
                )
                else -> StepValidationResult(
                    isValid = true,
                    canProceed = true,
                    completionPercentage = 1.0f,
                    nextAction = "Add Options",
                    helpText = "Great discount value!",
                )
            }
        }
        PromoCodeType.FIXED_AMOUNT -> {
            val amount = wizardData.discountAmount.toIntOrNull() ?: 0
            when {
                amount <= 0 -> StepValidationResult(
                    isValid = false,
                    canProceed = false,
                    completionPercentage = 0.0f,
                    errors = listOf("Please enter a discount amount."),
                    helpText = "Enter the fixed amount customers will save.",
                )
                amount > 10000 -> StepValidationResult(
                    isValid = true,
                    canProceed = true,
                    completionPercentage = 0.8f,
                    nextAction = "Add Options",
                    warnings = listOf("Large discount - ensure this aligns with your pricing strategy."),
                )
                else -> StepValidationResult(
                    isValid = true,
                    canProceed = true,
                    completionPercentage = 1.0f,
                    nextAction = "Add Options",
                    helpText = "Perfect discount amount!",
                )
            }
        }
        null -> StepValidationResult(
            isValid = false,
            canProceed = false,
            completionPercentage = 0.0f,
            errors = listOf("Please select a discount type first."),
            helpText = "Go back and choose percentage or fixed amount.",
        )
    }

private fun validateOptionsStep(wizardData: SubmissionWizardData): StepValidationResult =
    StepValidationResult(
        isValid = true,
        canProceed = true,
        completionPercentage = 1.0f,
        nextAction = "Set Start Date",
        helpText = "Options configured! Set when your promo becomes active.",
    )

private fun validateStartDateStep(wizardData: SubmissionWizardData): StepValidationResult =
    StepValidationResult(
        isValid = true,
        canProceed = true,
        completionPercentage = 1.0f,
        nextAction = "Set End Date",
        helpText = "Start date set! Now choose when it expires (optional).",
    )

private fun validateEndDateStep(wizardData: SubmissionWizardData): StepValidationResult =
    StepValidationResult(
        isValid = true,
        canProceed = true,
        completionPercentage = 1.0f,
        nextAction = "Submit Promo Code",
        helpText = "Ready to submit! Your promo code is complete.",
    )

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

@Preview(name = "Modern Floating Controller - With Context", showBackground = true)
@Composable
private fun ModernFloatingControllerPreview() {
    QodeTheme {
        ModernFloatingController(
            currentStep = ProgressiveStep.DISCOUNT_VALUE,
            wizardData = com.qodein.feature.promocode.submission.SubmissionWizardData(
                promoCodeType = com.qodein.feature.promocode.submission.PromoCodeType.PERCENTAGE,
                promoCode = "SAVE20",
                discountPercentage = "20",
            ),
            onNext = {},
            onPrevious = {},
            onQuickAction = { /* Handle quick action */ },
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}

@Preview(name = "Modern Floating Controller - Error State", showBackground = true)
@Composable
private fun ModernFloatingControllerErrorPreview() {
    QodeTheme {
        ModernFloatingController(
            currentStep = ProgressiveStep.PROMO_CODE,
            wizardData = com.qodein.feature.promocode.submission.SubmissionWizardData(
                promoCodeType = com.qodein.feature.promocode.submission.PromoCodeType.PERCENTAGE,
                promoCode = "AB", // Too short
            ),
            onNext = {},
            onPrevious = {},
            onQuickAction = { /* Handle quick action */ },
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}

@Preview(name = "Modern Floating Controller - Loading", showBackground = true)
@Composable
private fun ModernFloatingControllerLoadingPreview() {
    QodeTheme {
        ModernFloatingController(
            currentStep = ProgressiveStep.END_DATE,
            wizardData = com.qodein.feature.promocode.submission.SubmissionWizardData(
                promoCodeType = com.qodein.feature.promocode.submission.PromoCodeType.FIXED_AMOUNT,
                promoCode = "NEWBIE50",
                discountAmount = "500",
            ),
            isLoading = true,
            onNext = {},
            onPrevious = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
