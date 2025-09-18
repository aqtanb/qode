package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.feature.promocode.submission.ProgressiveStep
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData
import java.time.format.DateTimeFormatter

data class CompletedStepData(
    val step: ProgressiveStep,
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)

@Composable
fun SmartProgressSummary(
    currentStep: ProgressiveStep,
    wizardData: SubmissionWizardData,
    onEditStep: (ProgressiveStep) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isExpanded by remember { mutableStateOf(false) }

    val completedSteps = buildCompletedStepsList(
        currentStep = currentStep,
        wizardData = wizardData,
        primaryColor = MaterialTheme.colorScheme.primary,
        onPrimaryColor = MaterialTheme.colorScheme.onPrimary,
        secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        onSecondaryContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
        tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
        onTertiaryContainerColor = MaterialTheme.colorScheme.onTertiaryContainer,
        primaryContainerColor = MaterialTheme.colorScheme.primaryContainer,
        onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "chevronRotation",
    )

    AnimatedVisibility(
        visible = completedSteps.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = ElevationTokens.small,
            ),
        ) {
            Column {
                // Header with toggle
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isExpanded = !isExpanded
                        },
                    color = Color.Transparent,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.lg),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Progress Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${completedSteps.size} steps completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isExpanded = !isExpanded
                            },
                        ) {
                            Icon(
                                imageVector = QodeActionIcons.Down,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(SizeTokens.Icon.sizeMedium)
                                    .rotate(rotationAngle),
                            )
                        }
                    }
                }

                // Expandable content
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = SpacingTokens.lg,
                            end = SpacingTokens.lg,
                            bottom = SpacingTokens.lg,
                        ),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    ) {
                        completedSteps.forEachIndexed { index, stepData ->
                            CompletedStepItem(
                                stepData = stepData,
                                onEdit = { onEditStep(stepData.step) },
                                isLast = index == completedSteps.size - 1,
                            )
                        }
                    }
                }

                // Compact preview when collapsed
                AnimatedVisibility(
                    visible = !isExpanded && completedSteps.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    CompactStepsPreview(
                        completedSteps = completedSteps.take(3),
                        totalSteps = completedSteps.size,
                        modifier = Modifier.padding(
                            start = SpacingTokens.lg,
                            end = SpacingTokens.lg,
                            bottom = SpacingTokens.lg,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedStepItem(
    stepData: CompletedStepData,
    onEdit: () -> Unit,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onEdit()
            },
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = ElevationTokens.small,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Step icon with gradient background
            Box(
                modifier = Modifier
                    .size(SizeTokens.Icon.sizeXLarge)
                    .background(
                        color = stepData.containerColor,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = stepData.icon,
                    contentDescription = null,
                    tint = stepData.contentColor,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
            }

            // Step content
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stepData.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stepData.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                stepData.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Edit button
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEdit()
                },
            ) {
                Icon(
                    imageVector = QodeActionIcons.Edit,
                    contentDescription = "Edit ${stepData.title}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
            }
        }
    }
}

@Composable
private fun CompactStepsPreview(
    completedSteps: List<CompletedStepData>,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        completedSteps.forEach { stepData ->
            Box(
                modifier = Modifier
                    .size(SizeTokens.Icon.sizeLarge)
                    .background(
                        color = stepData.containerColor,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = stepData.icon,
                    contentDescription = null,
                    tint = stepData.contentColor,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
            }
        }

        if (totalSteps > 3) {
            Box(
                modifier = Modifier
                    .size(SizeTokens.Icon.sizeLarge)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+${totalSteps - 3}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Tap to expand",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun buildCompletedStepsList(
    currentStep: ProgressiveStep,
    wizardData: SubmissionWizardData,
    primaryColor: Color,
    onPrimaryColor: Color,
    secondaryContainerColor: Color,
    onSecondaryContainerColor: Color,
    tertiaryContainerColor: Color,
    onTertiaryContainerColor: Color,
    primaryContainerColor: Color,
    onPrimaryContainerColor: Color
): List<CompletedStepData> {
    val steps = mutableListOf<CompletedStepData>()

    // Service
    if (currentStep.stepNumber > ProgressiveStep.SERVICE.stepNumber && wizardData.serviceName.isNotBlank()) {
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.SERVICE,
                title = "Service",
                value = wizardData.serviceName,
                icon = QodeCommerceIcons.Store,
                containerColor = primaryColor,
                contentColor = onPrimaryColor,
            ),
        )
    }

    // Discount Type
    if (currentStep.stepNumber > ProgressiveStep.DISCOUNT_TYPE.stepNumber && wizardData.promoCodeType != null) {
        val discountType = when (wizardData.promoCodeType) {
            PromoCodeType.PERCENTAGE -> "Percentage Discount"
            PromoCodeType.FIXED_AMOUNT -> "Fixed Amount Discount"
            null -> ""
        }
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.DISCOUNT_TYPE,
                title = "Discount Type",
                value = discountType,
                icon = QodeCommerceIcons.Sale,
                containerColor = secondaryContainerColor,
                contentColor = onSecondaryContainerColor,
            ),
        )
    }

    // Promo Code
    if (currentStep.stepNumber > ProgressiveStep.PROMO_CODE.stepNumber && wizardData.promoCode.isNotBlank()) {
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.PROMO_CODE,
                title = "Promo Code",
                value = wizardData.promoCode,
                icon = QodeCommerceIcons.PromoCode,
                containerColor = tertiaryContainerColor,
                contentColor = onTertiaryContainerColor,
            ),
        )
    }

    // Discount Value
    if (currentStep.stepNumber > ProgressiveStep.DISCOUNT_VALUE.stepNumber) {
        val discountValue = when (wizardData.promoCodeType) {
            PromoCodeType.PERCENTAGE -> "${wizardData.discountPercentage}%"
            PromoCodeType.FIXED_AMOUNT -> "${wizardData.discountAmount}₸"
            null -> ""
        }
        if (discountValue.isNotBlank()) {
            steps.add(
                CompletedStepData(
                    step = ProgressiveStep.DISCOUNT_VALUE,
                    title = "Discount Value",
                    value = discountValue,
                    subtitle = "Min order: ${wizardData.minimumOrderAmount}₸",
                    icon = QodeCommerceIcons.Dollar,
                    containerColor = secondaryContainerColor,
                    contentColor = onSecondaryContainerColor,
                ),
            )
        }
    }

    // Options
    if (currentStep.stepNumber > ProgressiveStep.OPTIONS.stepNumber) {
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.OPTIONS,
                title = "Options",
                value = if (wizardData.isFirstUserOnly) "First-time customers only" else "All customers",
                subtitle = if (wizardData.description.isNotBlank()) wizardData.description else null,
                icon = QodeNavigationIcons.Settings,
                containerColor = primaryContainerColor,
                contentColor = onPrimaryContainerColor,
            ),
        )
    }

    // Start Date
    if (currentStep.stepNumber > ProgressiveStep.START_DATE.stepNumber) {
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.START_DATE,
                title = "Start Date",
                value = wizardData.startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                icon = QodeUIIcons.Datepicker,
                containerColor = primaryContainerColor,
                contentColor = onPrimaryContainerColor,
            ),
        )
    }

    // End Date
    if (currentStep.stepNumber > ProgressiveStep.END_DATE.stepNumber && wizardData.endDate != null) {
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.END_DATE,
                title = "End Date",
                value = wizardData.endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                icon = QodeUIIcons.Datepicker,
                containerColor = primaryContainerColor,
                contentColor = onPrimaryContainerColor,
            ),
        )
    }

    return steps
}

@Preview(name = "Smart Progress Summary - Expanded", showBackground = true)
@Composable
private fun SmartProgressSummaryExpandedPreview() {
    QodeTheme {
        SmartProgressSummary(
            currentStep = ProgressiveStep.OPTIONS,
            wizardData = SubmissionWizardData(
                selectedService = ServicePreviewData.netflix,
                promoCodeType = PromoCodeType.PERCENTAGE,
                promoCode = "SAVE20",
                discountPercentage = "20",
                minimumOrderAmount = "500",
            ),
            onEditStep = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
