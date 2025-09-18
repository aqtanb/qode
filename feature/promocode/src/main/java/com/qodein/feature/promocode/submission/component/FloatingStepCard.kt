package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.ProgressiveStep
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .animateContentSize(),
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
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
            // Step header with icon and title
            StepHeader(
                step = currentStep,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.lg))

            // Animated step content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut(),
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
private fun StepHeader(
    step: ProgressiveStep,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Step icon with material background
        Box(
            modifier = Modifier
                .size(SizeTokens.Icon.sizeXLarge + 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(ShapeTokens.Corner.medium),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = getStepIcon(step),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
    }
}

private fun getStepIcon(step: ProgressiveStep): ImageVector =
    when (step) {
        ProgressiveStep.SERVICE -> QodeCommerceIcons.Store
        ProgressiveStep.DISCOUNT_TYPE -> QodeCommerceIcons.Sale
        ProgressiveStep.PROMO_CODE -> QodeCommerceIcons.PromoCode
        ProgressiveStep.DISCOUNT_VALUE -> QodeCommerceIcons.Dollar
        ProgressiveStep.OPTIONS -> QodeNavigationIcons.Settings
        ProgressiveStep.START_DATE, ProgressiveStep.END_DATE -> QodeUIIcons.Datepicker
    }

private val ProgressiveStep.title: String
    get() = when (this) {
        ProgressiveStep.SERVICE -> "Choose Service"
        ProgressiveStep.DISCOUNT_TYPE -> "Discount Type"
        ProgressiveStep.PROMO_CODE -> "Promo Code"
        ProgressiveStep.DISCOUNT_VALUE -> "Set Value"
        ProgressiveStep.OPTIONS -> "Additional Options"
        ProgressiveStep.START_DATE -> "Start Date"
        ProgressiveStep.END_DATE -> "End Date"
    }

private val ProgressiveStep.subtitle: String
    get() = when (this) {
        ProgressiveStep.SERVICE -> "Select the service for your promo code"
        ProgressiveStep.DISCOUNT_TYPE -> "Choose percentage or fixed amount"
        ProgressiveStep.PROMO_CODE -> "Enter your promotional code"
        ProgressiveStep.DISCOUNT_VALUE -> "Set discount amount and minimum order"
        ProgressiveStep.OPTIONS -> "Configure who can use this code"
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
        ProgressiveStep.OPTIONS -> "Decide who can use this code"
        ProgressiveStep.START_DATE -> "Choose when customers can start using it"
        ProgressiveStep.END_DATE -> "Set when the code should expire"
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
