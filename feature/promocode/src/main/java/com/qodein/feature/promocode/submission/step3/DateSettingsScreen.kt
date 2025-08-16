package com.qodein.feature.promocode.submission.step3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSettingsScreen(
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Text(
            text = "Date Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Set when your promo code will be active",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        QodeCard(variant = QodeCardVariant.Outlined) {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
            ) {
                Text(
                    text = "Validity Period",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                // Start Date
                DatePickerField(
                    label = "Start Date",
                    selectedDate = wizardData.startDate,
                    onDateClick = { showStartDatePicker = true },
                    onClearClick = null, // Start date is required
                    placeholder = "Today (default)",
                )

                // End Date
                DatePickerField(
                    label = "End Date",
                    selectedDate = wizardData.endDate,
                    onDateClick = { showEndDatePicker = true },
                    onClearClick = { /* End date is required, no clear option */ },
                    placeholder = "Select end date",
                    isRequired = true,
                )

                // Date validation message
                if (wizardData.endDate.isBefore(wizardData.startDate) ||
                    wizardData.endDate == wizardData.startDate
                ) {
                    Text(
                        text = "End date must be after start date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        // Helpful hint about dates
        Text(
            text = "💡 Promo code will be active from start date to end date",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onAction(SubmissionWizardAction.UpdateStartDate(date))
                        }
                        showStartDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onAction(SubmissionWizardAction.UpdateEndDate(date))
                        }
                        showEndDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    selectedDate: LocalDate,
    onDateClick: () -> Unit,
    onClearClick: (() -> Unit)?,
    placeholder: String,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = if (isRequired) "$label *" else label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.xs))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onDateClick() },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (onClearClick != null) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DateSettingsScreenPreview() {
    QodeTheme {
        DateSettingsScreen(
            wizardData = SubmissionWizardData(
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(30),
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DateSettingsScreenEmptyPreview() {
    QodeTheme {
        DateSettingsScreen(
            wizardData = SubmissionWizardData(),
            onAction = {},
        )
    }
}
