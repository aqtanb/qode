package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData

@Composable
fun AdditionalOptionsCard(
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        // Clean layout with subtle background - matching other sections
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(ShapeTokens.Corner.large),
                )
                .padding(SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Improved checkbox layout with better alignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
                verticalAlignment = Alignment.Top,
            ) {
                // Custom styled checkbox
                Checkbox(
                    checked = wizardData.isFirstUserOnly,
                    onCheckedChange = { onAction(SubmissionWizardAction.UpdateFirstUserOnly(it)) },
                    modifier = Modifier.padding(top = SpacingTokens.xs),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "First-time customers only",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = "Restrict to new customers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = SpacingTokens.xs),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Description field with consistent styling
            QodeTextField(
                value = wizardData.description,
                onValueChange = { onAction(SubmissionWizardAction.UpdateDescription(it)) },
                label = "Description (Optional)",
                placeholder = "Add details about this promo code...",
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
            )
        }
    }
}

@Preview(name = "Additional Options Card - Empty", showBackground = true)
@Composable
private fun AdditionalOptionsCardEmptyPreview() {
    QodeTheme {
        AdditionalOptionsCard(
            wizardData = SubmissionWizardData(),
            onAction = {},
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}

@Preview(name = "Additional Options Card - Filled", showBackground = true)
@Composable
private fun AdditionalOptionsCardFilledPreview() {
    QodeTheme {
        AdditionalOptionsCard(
            wizardData = SubmissionWizardData(
                isFirstUserOnly = true,
                description = "Great deal for streaming services! Get 20% off your first subscription.",
            ),
            onAction = {},
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}
