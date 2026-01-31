package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.icon.QodeCalendarIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.PromocodeSubmissionScreen
import com.qodein.feature.promocode.submission.PromocodeSubmissionUiState
import com.qodein.feature.promocode.submission.PromocodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.qodein.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocodeDatesStep(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = stringResource(R.string.promocode_dates_start_choose_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = SpacingTokens.md, start = SpacingTokens.xs),
        )

        DatePickerField(
            selectedDate = startDate,
            onDateSelected = onStartDateSelected,
            placeholder = stringResource(R.string.promocode_dates_start_placeholder),
        )

        Text(
            text = stringResource(R.string.promocode_dates_end_choose_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = SpacingTokens.md, top = SpacingTokens.xl, start = SpacingTokens.xs),
        )

        DatePickerField(
            selectedDate = endDate,
            onDateSelected = onEndDateSelected,
            placeholder = stringResource(R.string.promocode_dates_end_placeholder),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val interactionSource = remember { MutableInteractionSource() }
    val hasSelection = selectedDate != null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(SizeTokens.Selector.height)
            .border(
                width = ShapeTokens.Border.thin,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(SizeTokens.Selector.shape),
            )
            .clip(RoundedCornerShape(SizeTokens.Selector.shape))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { showDatePicker = true },
            ),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(SizeTokens.Selector.shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SizeTokens.Selector.padding),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = QodeCalendarIcons.Datepicker,
                contentDescription = null,
                modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                tint = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = selectedDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: placeholder,
                fontWeight = if (hasSelection) FontWeight.SemiBold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onDateSelected(date)
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.promocode_dates_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(CoreUiR.string.ui_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@PreviewLightDark
@Composable
private fun DatesStepPreview() {
    QodeTheme {
        PromocodeSubmissionScreen(
            uiState = PromocodeSubmissionUiState(
                currentStep = PromocodeWizardStep.DATES,
                wizardData = SubmissionWizardData(
                    selectedService = ServicePreviewData.netflix,
                    promocodeType = PromocodeType.PERCENTAGE,
                ),
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
        )
    }
}
