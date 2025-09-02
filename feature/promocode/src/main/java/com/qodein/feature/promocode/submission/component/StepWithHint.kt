package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.ProgressiveStep

@Composable
fun StepWithHint(
    currentStep: ProgressiveStep,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + scaleIn(
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        ),
        exit = fadeOut(tween(300)) + scaleOut(tween(300)),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = SpacingTokens.xl),
        ) {
            // Hint card stays at consistent position
            HintCard(
                text = currentStep.hint,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.lg))

            // Current step content centered in remaining space
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}

@Preview(name = "Centered Current Step", showSystemUi = true)
@Composable
private fun CenteredCurrentStepPreview() {
    QodeTheme {
        StepWithHint(
            currentStep = ProgressiveStep.SERVICE,
        ) {
            // Sample content - would be actual input component
            Text("Sample Step Content")
        }
    }
}
