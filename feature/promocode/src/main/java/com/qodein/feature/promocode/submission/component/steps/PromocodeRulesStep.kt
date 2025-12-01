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
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.component.SubmissionFieldOption

@Composable
internal fun PromocodeRulesStep(
    isFirstUserOnly: Boolean,
    isOneTimeUseOnly: Boolean,
    onFirstUserOnlyChange: (Boolean) -> Unit,
    onOneTimeUseOnlyChange: (Boolean) -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // First user only toggle
        val options = listOf(
            SubmissionFieldOption(
                value = "all",
                label = "All Customers",
                description = "Any customer can use this promo code",
            ),
            SubmissionFieldOption(
                value = "first",
                label = "First-time Customers Only",
                description = "Only new customers can use this code",
            ),
        )

        // Simple toggle for customer eligibility
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Text(
                text = "Customer Eligibility",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            options.forEach { option ->
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .clickable {
                            onFirstUserOnlyChange(option.value == "first")
                        }
                        .padding(SpacingTokens.sm),
                    verticalAlignment = Alignment.Companion.CenterVertically,
                ) {
                    RadioButton(
                        selected = (isFirstUserOnly && option.value == "first") || (!isFirstUserOnly && option.value == "all"),
                        onClick = { onFirstUserOnlyChange(option.value == "first") },
                    )
                    Spacer(modifier = Modifier.Companion.width(SpacingTokens.sm))
                    Column {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        option.description?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Usage Limitation Group
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Text(
                text = "Usage Limitation",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            val usageOptions = listOf(
                SubmissionFieldOption(
                    value = "multiple",
                    label = "Multiple uses",
                    description = "Can be used multiple times",
                ),
                SubmissionFieldOption(
                    value = "oneTime",
                    label = "One-time use only",
                    description = "Code gets deleted after first use",
                ),
            )

            usageOptions.forEach { option ->
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .clickable {
                            onOneTimeUseOnlyChange(option.value == "oneTime")
                        }
                        .padding(SpacingTokens.sm),
                    verticalAlignment = Alignment.Companion.CenterVertically,
                ) {
                    RadioButton(
                        selected = (isOneTimeUseOnly && option.value == "oneTime") || (!isOneTimeUseOnly && option.value == "multiple"),
                        onClick = { onOneTimeUseOnlyChange(option.value == "oneTime") },
                    )
                    Spacer(modifier = Modifier.Companion.width(SpacingTokens.sm))
                    Column {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        option.description?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
