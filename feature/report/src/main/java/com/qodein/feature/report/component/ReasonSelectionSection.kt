package com.qodein.feature.report.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.report.R
import com.qodein.shared.model.ReportReason

@Composable
internal fun ReasonSelectionSection(
    selectedReason: ReportReason?,
    onReasonSelected: (ReportReason) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        Text(
            text = stringResource(R.string.select_reason_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ReportReason.entries.forEach { reason ->
            ReasonRadioItem(
                reason = reason,
                isSelected = selectedReason == reason,
                onClick = { onReasonSelected(reason) },
            )
        }
    }
}

@Composable
private fun ReasonRadioItem(
    reason: ReportReason,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
        )

        Column(
            modifier = Modifier.padding(start = SpacingTokens.sm),
        ) {
            Text(
                text = stringResource(getReasonTitleRes(reason)),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(getReasonDescriptionRes(reason)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun getReasonTitleRes(reason: ReportReason): Int =
    when (reason) {
        ReportReason.SPAM -> R.string.reason_spam
        ReportReason.SCAM_OR_MISLEADING -> R.string.reason_scam
        ReportReason.INAPPROPRIATE_CONTENT -> R.string.reason_inappropriate
        ReportReason.MALICIOUS_LINK -> R.string.reason_malicious
        ReportReason.OTHER -> R.string.reason_other
    }

private fun getReasonDescriptionRes(reason: ReportReason): Int =
    when (reason) {
        ReportReason.SPAM -> R.string.reason_spam_desc
        ReportReason.SCAM_OR_MISLEADING -> R.string.reason_scam_desc
        ReportReason.INAPPROPRIATE_CONTENT -> R.string.reason_inappropriate_desc
        ReportReason.MALICIOUS_LINK -> R.string.reason_malicious_desc
        ReportReason.OTHER -> R.string.reason_other_desc
    }
