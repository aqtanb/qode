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
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.icon.QodeCalendarIcons
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromocodeSubmissionAction
import com.qodein.feature.promocode.submission.PromocodeSubmissionStep
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.hintRes
import com.qodein.feature.promocode.submission.titleRes

@Composable
fun PromocodeSubmissionCard(
    currentStep: PromocodeSubmissionStep,
    wizardData: SubmissionWizardData,
    onAction: (PromocodeSubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var isHintExpanded by remember { mutableStateOf(false) }

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
                        .padding(top = SpacingTokens.sm),
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
                PromocodeSubmissionCardContent(
                    currentStep = step,
                    wizardData = wizardData,
                    onAction = onAction,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StepHeader(
    step: PromocodeSubmissionStep,
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
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = SpacingTokens.sm),
        )

        IconButton(
            onClick = onHintToggle,
            modifier = Modifier.size(SizeTokens.IconButton.sizeSmall),
        ) {
            Icon(
                imageVector = if (isHintExpanded) {
                    UIIcons.HintFilled
                } else {
                    UIIcons.Hint
                },
                contentDescription = "Toggle helpful tips",
                tint = if (isHintExpanded) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun HintCard(
    step: PromocodeSubmissionStep,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Icon(
                imageVector = QodeCalendarIcons.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
            )

            Text(
                text = stringResource(step.hintRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun PromocodeSubmissionCardPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeSubmissionStep.SERVICE,
            wizardData = SubmissionWizardData(),
            onAction = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
