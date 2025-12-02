package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.validation.ValidationState
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep

private data class SubmissionFieldOption(val value: String, val label: String, val description: String? = null)

@Composable
internal fun PromocodeRulesStep(
    isFirstUserOnly: Boolean,
    isOneTimeUseOnly: Boolean,
    onFirstUserOnlyChange: (Boolean) -> Unit,
    onOneTimeUseOnlyChange: (Boolean) -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        val eligibilityOptions = listOf(
            SubmissionFieldOption(
                value = "all",
                label = "All customers",
                description = "Any customer can use this promo code",
            ),
            SubmissionFieldOption(
                value = "first",
                label = "First-time customers only",
                description = "Only new customers can use this code",
            ),
        )

        val usageOptions = listOf(
            SubmissionFieldOption(
                value = "multiple",
                label = "Multiple uses",
                description = "Customers can redeem this code more than once",
            ),
            SubmissionFieldOption(
                value = "oneTime",
                label = "One-time use",
                description = "Automatically expires after a single redemption",
            ),
        )

        OptionRadioGroup(
            title = "Customer Eligibility",
            options = eligibilityOptions,
            selectedValue = if (isFirstUserOnly) "first" else "all",
            onOptionSelected = { onFirstUserOnlyChange(it == "first") },
            focusRequester = focusRequester,
        )

        OptionRadioGroup(
            title = "Usage Limitation",
            options = usageOptions,
            selectedValue = if (isOneTimeUseOnly) "oneTime" else "multiple",
            onOptionSelected = { onOneTimeUseOnlyChange(it == "oneTime") },
        )
    }
}

@Composable
private fun OptionRadioGroup(
    title: String,
    options: List<SubmissionFieldOption>,
    selectedValue: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    Column(
        modifier = modifier.padding(horizontal = SpacingTokens.sm),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        options.forEachIndexed { index, option ->
            val optionModifier = Modifier
                .fillMaxWidth()
                .then(
                    if (index == 0 && focusRequester != null) {
                        Modifier.focusRequester(focusRequester)
                    } else {
                        Modifier
                    },
                )
                .clickable(
                    role = Role.RadioButton,
                    onClick = { onOptionSelected(option.value) },
                )
                .padding(
                    horizontal = SpacingTokens.sm,
                    vertical = SpacingTokens.xs,
                )

            Row(
                modifier = optionModifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = option.value == selectedValue,
                    onClick = { onOptionSelected(option.value) },
                )
                Spacer(modifier = Modifier.width(SpacingTokens.sm))
                Column {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    option.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun PromocodeRulesStep() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeSubmissionStep.RULES,
            wizardData = SubmissionWizardData(),
            validation = ValidationState.valid(),
            onAction = {},
        )
    }
}
