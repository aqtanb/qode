package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromocodeSubmissionAction
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.validation.ValidationState
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import com.qodein.feature.promocode.submission.wizard.titleRes

@Composable
fun PromocodeSubmissionCard(
    currentStep: PromocodeWizardStep,
    wizardData: SubmissionWizardData,
    validation: ValidationState,
    onAction: (PromocodeSubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(ShapeTokens.Corner.small),
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
            StepHeader(
                step = currentStep,
                modifier = Modifier.fillMaxWidth().padding(bottom = SpacingTokens.sm),
            )

            AnimatedContent(
                modifier = Modifier.padding(top = SpacingTokens.lg),
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width / 3 } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width / 3 } + fadeOut()
                },
                label = "stepContent",
            ) { step ->
                PromocodeSubmissionCardContent(
                    currentStep = step,
                    wizardData = wizardData,
                    validation = validation,
                    onAction = onAction,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StepHeader(
    step: PromocodeWizardStep,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(step.titleRes),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = SpacingTokens.sm),
        )
    }
}

@ThemePreviews
@Composable
private fun PromocodeSubmissionCardPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeWizardStep.SERVICE,
            wizardData = SubmissionWizardData(),
            validation = ValidationState.valid(),
            onAction = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
