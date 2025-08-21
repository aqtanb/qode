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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.qodein.core.analytics.TrackScreenViewEvent
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
    TrackScreenViewEvent(screenName = "SubmissionWizard_DateSettings")

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxl),
    ) {
        // Start Date
        DatePickerField(
            label = "Start Date",
            selectedDate = wizardData.startDate,
            onDateClick = { showStartDatePicker = true },
            onClearClick = null,
            placeholder = "Today (default)",
            isRequired = true,
        )

        // End Date
        DatePickerField(
            label = "End Date",
            selectedDate = wizardData.endDate,
            onDateClick = { showEndDatePicker = true },
            onClearClick = null,
            placeholder = "Select end date",
            isRequired = true,
        )

        // Date validation message
        if (wizardData.endDate != null &&
            (wizardData.endDate.isBefore(wizardData.startDate) || wizardData.endDate == wizardData.startDate)
        ) {
            Text(
                text = "End date must be after start date",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = SpacingTokens.xs),
            )
        }
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
    selectedDate: LocalDate?,
    onDateClick: () -> Unit,
    onClearClick: (() -> Unit)?,
    placeholder: String,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = if (isRequired) "$label *" else label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.md))

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDateClick() },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedDate?.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")) ?: placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select date",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// MARK: - Enterprise-Level Previews

@Preview(name = "Date Settings - Default State", showBackground = true)
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

@Preview(name = "Date Settings - Empty State", showBackground = true)
@Composable
private fun DateSettingsScreenEmptyPreview() {
    QodeTheme {
        DateSettingsScreen(
            wizardData = SubmissionWizardData(),
            onAction = {},
        )
    }
}

@Preview(name = "Date Settings - Validation Error", showBackground = true)
@Composable
private fun DateSettingsScreenValidationErrorPreview() {
    QodeTheme {
        DateSettingsScreen(
            wizardData = SubmissionWizardData(
                startDate = LocalDate.now().plusDays(10),
                endDate = LocalDate.now().plusDays(5), // End before start
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Date Settings - Extended Period", showBackground = true)
@Composable
private fun DateSettingsScreenExtendedPreview() {
    QodeTheme {
        DateSettingsScreen(
            wizardData = SubmissionWizardData(
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(90),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Date Settings - Dark Theme", showBackground = true)
@Composable
private fun DateSettingsScreenDarkPreview() {
    QodeTheme(darkTheme = true) {
        DateSettingsScreen(
            wizardData = SubmissionWizardData(
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(60),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Date Picker Field - Variants", showBackground = true)
@Composable
private fun DatePickerFieldPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            DatePickerField(
                label = "Start Date",
                selectedDate = LocalDate.now(),
                onDateClick = {},
                onClearClick = null,
                placeholder = "Select start date",
                isRequired = false,
            )
            DatePickerField(
                label = "End Date",
                selectedDate = LocalDate.now().plusWeeks(2),
                onDateClick = {},
                onClearClick = null,
                placeholder = "Select end date",
                isRequired = true,
            )
        }
    }
}
