package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.ProgressiveStep
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData
import java.time.format.DateTimeFormatter

@Composable
fun StepsStack(
    currentStep: ProgressiveStep,
    wizardData: SubmissionWizardData,
    modifier: Modifier = Modifier
) {
    val stackItems = buildList {
        // Service
        if (currentStep.stepNumber > ProgressiveStep.SERVICE.stepNumber) {
            add(
                StackItemData(
                    label = "Service",
                    value = wizardData.serviceName,
                ),
            )
        }

        // Discount Type
        if (currentStep.stepNumber > ProgressiveStep.DISCOUNT_TYPE.stepNumber) {
            val discountType = when (wizardData.promoCodeType) {
                PromoCodeType.PERCENTAGE -> "Percentage"
                PromoCodeType.FIXED_AMOUNT -> "Fixed Amount"
                null -> ""
            }
            add(
                StackItemData(
                    label = "Type",
                    value = discountType,
                ),
            )
        }

        // Promo Code
        if (currentStep.stepNumber > ProgressiveStep.PROMO_CODE.stepNumber) {
            add(
                StackItemData(
                    label = "Code",
                    value = wizardData.promoCode,
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
            add(
                StackItemData(
                    label = "Discount",
                    value = "$discountValue (Min: ${wizardData.minimumOrderAmount}₸)",
                ),
            )
        }

        // Options
        if (currentStep.stepNumber > ProgressiveStep.OPTIONS.stepNumber) {
            add(
                StackItemData(
                    label = "Options",
                    value = if (wizardData.isFirstUserOnly) "First-time customers only" else "All customers",
                ),
            )
        }

        // Start Date
        if (currentStep.stepNumber > ProgressiveStep.START_DATE.stepNumber) {
            add(
                StackItemData(
                    label = "Start Date",
                    value = wizardData.startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                ),
            )
        }

        // End Date
        if (currentStep.stepNumber > ProgressiveStep.END_DATE.stepNumber) {
            add(
                StackItemData(
                    label = "End Date",
                    value = wizardData.endDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "",
                ),
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SpacingTokens.massive * 2),
    ) {
        if (stackItems.isEmpty()) {
            EmptyStackState(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = SpacingTokens.lg),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = SpacingTokens.lg),
            ) {
                Spacer(modifier = Modifier.height(SpacingTokens.lg))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    stackItems.forEach { item ->
                        StackItem(
                            label = item.label,
                            value = item.value,
                            isVisible = true,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.md))
            }
        }
    }
}

@Composable
private fun EmptyStackState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        Text(
            text = "Complete steps to see your progress",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun StackItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
        exit = fadeOut(tween(300)) + shrinkVertically(tween(300)),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                    RoundedCornerShape(ShapeTokens.Corner.large),
                )
                .padding(SpacingTokens.lg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = QodeActionIcons.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )

            Column(modifier = modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview(name = "Stack Item", showBackground = true)
@Composable
private fun StackItemPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            StackItem(
                label = "Service",
                value = "Netflix",
            )
            StackItem(
                label = "Discount Type",
                value = "Percentage",
            )
            StackItem(
                label = "Promo Code",
                value = "SAVE20",
            )
        }
    }
}
private data class StackItemData(val label: String, val value: String)
