package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.ProgressiveStep
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.ServiceSelectionUiState
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData

@Composable
fun CurrentStepContent(
    currentStep: ProgressiveStep,
    wizardData: SubmissionWizardData,
    serviceSelectionUiState: ServiceSelectionUiState,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    when (currentStep) {
        ProgressiveStep.SERVICE -> {
            ServiceSelectionCard(
                serviceName = wizardData.serviceName,
                showManualEntry = serviceSelectionUiState is ServiceSelectionUiState.ManualEntry,
                onServiceNameChange = { onAction(SubmissionWizardAction.UpdateServiceName(it)) },
                onSelectService = { onAction(SubmissionWizardAction.ShowServiceSelector) },
                onToggleManualEntry = { onAction(SubmissionWizardAction.ToggleManualEntry) },
                modifier = modifier,
            )
        }

        ProgressiveStep.DISCOUNT_TYPE -> {
            PromoCodeTypeSelector(
                selectedType = wizardData.promoCodeType,
                onTypeSelected = { onAction(SubmissionWizardAction.UpdatePromoCodeType(it)) },
                modifier = modifier,
            )
        }

        ProgressiveStep.PROMO_CODE -> {
            QodeTextField(
                value = wizardData.promoCode,
                onValueChange = { onAction(SubmissionWizardAction.UpdatePromoCode(it)) },
                label = "Promo Code",
                placeholder = "SAVE20",
                modifier = modifier.fillMaxWidth(),
                required = true,
            )
        }

        ProgressiveStep.DISCOUNT_VALUE -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
                ) {
                    // Discount field
                    when (wizardData.promoCodeType) {
                        PromoCodeType.PERCENTAGE -> {
                            QodeTextField(
                                value = wizardData.discountPercentage,
                                onValueChange = { onAction(SubmissionWizardAction.UpdateDiscountPercentage(it)) },
                                label = "Percentage",
                                placeholder = "20",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                required = true,
                            )
                        }
                        PromoCodeType.FIXED_AMOUNT -> {
                            QodeTextField(
                                value = wizardData.discountAmount,
                                onValueChange = { onAction(SubmissionWizardAction.UpdateDiscountAmount(it)) },
                                label = "Amount (₸)",
                                placeholder = "100",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                required = true,
                            )
                        }
                        null -> Unit
                    }

                    // Min order field
                    QodeTextField(
                        value = wizardData.minimumOrderAmount,
                        onValueChange = { onAction(SubmissionWizardAction.UpdateMinimumOrderAmount(it)) },
                        label = "Min Order (₸)",
                        placeholder = "500",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        required = true,
                    )
                }
            }
        }

        ProgressiveStep.OPTIONS -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
            ) {
                // First user only checkbox
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = wizardData.isFirstUserOnly,
                        onCheckedChange = { onAction(SubmissionWizardAction.UpdateFirstUserOnly(it)) },
                    )
                    Text(
                        text = "First-time customers only",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Description
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

        ProgressiveStep.START_DATE -> {
            DatePickerStep(
                label = "Start Date",
                selectedDate = wizardData.startDate,
                onDateSelected = { onAction(SubmissionWizardAction.UpdateStartDate(it)) },
                modifier = modifier,
            )
        }

        ProgressiveStep.END_DATE -> {
            DatePickerStep(
                label = "End Date",
                selectedDate = wizardData.endDate,
                onDateSelected = { onAction(SubmissionWizardAction.UpdateEndDate(it)) },
                placeholder = "Select end date",
                isRequired = true,
                modifier = modifier,
            )
        }
    }
}

@Preview(name = "Current Step Content - Service", showBackground = true)
@Composable
private fun CurrentStepContentServicePreview() {
    QodeTheme {
        CurrentStepContent(
            currentStep = ProgressiveStep.SERVICE,
            wizardData = SubmissionWizardData(),
            serviceSelectionUiState = ServiceSelectionUiState.Default,
            onAction = {},
        )
    }
}

@Preview(name = "Current Step Content - Promo Type", showBackground = true)
@Composable
private fun CurrentStepContentPromoTypePreview() {
    QodeTheme {
        CurrentStepContent(
            currentStep = ProgressiveStep.DISCOUNT_TYPE,
            wizardData = SubmissionWizardData(),
            serviceSelectionUiState = ServiceSelectionUiState.Default,
            onAction = {},
        )
    }
}
