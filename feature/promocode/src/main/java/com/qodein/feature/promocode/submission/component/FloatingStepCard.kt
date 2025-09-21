package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.ProgressiveStep
import com.qodein.feature.promocode.submission.ServiceSelectionUiState
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.titleRes

@Composable
fun FloatingStepCard(
    currentStep: ProgressiveStep,
    wizardData: SubmissionWizardData,
    serviceSelectionUiState: ServiceSelectionUiState,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var isHintExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = ShapeTokens.Border.thin,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.xl),
        ) {
            // Clean header with proper title
            StepHeader(
                step = currentStep,
                isHintExpanded = isHintExpanded,
                onHintToggle = { isHintExpanded = !isHintExpanded },
                modifier = Modifier.fillMaxWidth().padding(bottom = SpacingTokens.sm),
            )

            AnimatedVisibility(
                visible = isHintExpanded,
                enter = expandVertically(
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
                    expandFrom = Alignment.Top,
                ) + fadeIn(animationSpec = spring(dampingRatio = 0.75f)),
                exit = shrinkVertically(
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(animationSpec = spring(dampingRatio = 0.75f)),
            ) {
                HintCard(
                    step = currentStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SpacingTokens.lg),
                )
            }

            AnimatedContent(
                modifier = Modifier.padding(top = SpacingTokens.lg),
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width / 3 } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width / 3 } + fadeOut()
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
    isHintExpanded: Boolean,
    onHintToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(step.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(end = SpacingTokens.sm),
        )

        IconButton(
            onClick = onHintToggle,
            modifier = Modifier.size(SizeTokens.IconButton.sizeSmall),
        ) {
            Icon(
                imageVector = QodeUIIcons.Info,
                contentDescription = "Toggle helpful tips",
                modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                tint = if (isHintExpanded) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun HintCard(
    step: ProgressiveStep,
    modifier: Modifier = Modifier
) {
    // Using your beautiful StepWithInstructions pattern
    Card(
        modifier = modifier,
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
                text = step.hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
