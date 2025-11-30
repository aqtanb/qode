package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromocodeSubmissionAction
import com.qodein.feature.promocode.submission.PromocodeSubmissionStep
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.steps.DiscountValueStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeDatesStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeTypeStep
import com.qodein.feature.promocode.submission.component.steps.ServiceStep
import com.qodein.feature.promocode.submission.validation.getBusinessLogicValidationError
import kotlinx.coroutines.delay
import java.time.LocalDate

// Simple UI validation states for field feedback
internal enum class FieldValidationState {
    IDLE,
    VALID,
    ERROR
}

@Composable
fun SubmissionWizardStepContent(
    currentStep: PromocodeSubmissionStep,
    wizardData: SubmissionWizardData,
    onAction: (PromocodeSubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Auto-focus when step changes
    LaunchedEffect(currentStep) {
        delay(300) // Wait for animations to settle
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        when (currentStep) {
            PromocodeSubmissionStep.SERVICE -> ServiceStep(
                selectedService = wizardData.selectedService,
                serviceName = wizardData.serviceName,
                isManualEntry = wizardData.isManualServiceEntry,
                onShowServiceSelector = { onAction(PromocodeSubmissionAction.ShowServiceSelector) },
                onServiceNameChange = { onAction(PromocodeSubmissionAction.UpdateServiceName(it)) },
                onToggleManualEntry = { onAction(PromocodeSubmissionAction.ToggleManualEntry) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeSubmissionStep.DISCOUNT_TYPE -> PromocodeTypeStep(
                selectedType = wizardData.promocodeType,
                onTypeSelected = { onAction(PromocodeSubmissionAction.UpdatePromocodeType(it)) },
            )

            PromocodeSubmissionStep.PROMOCODE -> PromocodeStep(
                promoCode = wizardData.promoCode,
                onPromoCodeChange = { onAction(PromocodeSubmissionAction.UpdatePromoCode(it)) },
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeSubmissionStep.DISCOUNT_VALUE -> DiscountValueStep(
                promoCodeType = wizardData.promocodeType,
                discountPercentage = wizardData.discountPercentage,
                discountAmount = wizardData.discountAmount,
                onDiscountPercentageChange = { onAction(PromocodeSubmissionAction.UpdateDiscountPercentage(it)) },
                onDiscountAmountChange = { onAction(PromocodeSubmissionAction.UpdateDiscountAmount(it)) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeSubmissionStep.MINIMUM_ORDER -> MinimumOrderStepContent(
                minimumOrderAmount = wizardData.minimumOrderAmount,
                onMinimumOrderAmountChange = { onAction(PromocodeSubmissionAction.UpdateMinimumOrderAmount(it)) },
                focusRequester = focusRequester,
                wizardData = wizardData,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeSubmissionStep.ELIGIBILITY -> EligibilityStepContent(
                isFirstUserOnly = wizardData.isFirstUserOnly,
                isOneTimeUseOnly = wizardData.isOneTimeUseOnly,
                onFirstUserOnlyChange = { onAction(PromocodeSubmissionAction.UpdateFirstUserOnly(it)) },
                onOneTimeUseOnlyChange = { onAction(PromocodeSubmissionAction.UpdateOneTimeUseOnly(it)) },
                focusRequester = focusRequester,
            )

            PromocodeSubmissionStep.DESCRIPTION -> DescriptionStepContent(
                description = wizardData.description,
                onDescriptionChange = { onAction(PromocodeSubmissionAction.UpdateDescription(it)) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.SubmitPromoCode) },
            )

            PromocodeSubmissionStep.START_DATE -> StartDateStepContent(
                startDate = wizardData.startDate,
                onDateSelected = { onAction(PromocodeSubmissionAction.UpdateStartDate(it)) },
            )

            PromocodeSubmissionStep.END_DATE -> EndDateStepContent(
                endDate = wizardData.endDate,
                onDateSelected = { date ->
                    if (date != null) {
                        onAction(PromocodeSubmissionAction.UpdateEndDate(date))
                    }
                },
            )
        }
    }
}

@Composable
private fun MinimumOrderStepContent(
    minimumOrderAmount: String,
    onMinimumOrderAmountChange: (String) -> Unit,
    focusRequester: FocusRequester,
    wizardData: SubmissionWizardData,
    onNextStep: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        SubmissionTextField(
            value = minimumOrderAmount,
            onValueChange = onMinimumOrderAmountChange,
            label = "Minimum Order Amount",
            placeholder = "1000",
            fieldType = SubmissionFieldType.CURRENCY,
            leadingIcon = QodeIcons.Dollar,
            helperText = "Minimum order value required to apply this discount",
            errorText = getBusinessLogicValidationError(wizardData),
            isRequired = true,
            focusRequester = focusRequester,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number,
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNextStep() },
            ),
        )
    }
}

@Composable
private fun EligibilityStepContent(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onFirstUserOnlyChange(option.value == "first")
                        }
                        .padding(SpacingTokens.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = (isFirstUserOnly && option.value == "first") || (!isFirstUserOnly && option.value == "all"),
                        onClick = { onFirstUserOnlyChange(option.value == "first") },
                    )
                    Spacer(modifier = Modifier.width(SpacingTokens.sm))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onOneTimeUseOnlyChange(option.value == "oneTime")
                        }
                        .padding(SpacingTokens.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = (isOneTimeUseOnly && option.value == "oneTime") || (!isOneTimeUseOnly && option.value == "multiple"),
                        onClick = { onOneTimeUseOnlyChange(option.value == "oneTime") },
                    )
                    Spacer(modifier = Modifier.width(SpacingTokens.sm))
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

@Composable
private fun DescriptionStepContent(
    description: String,
    onDescriptionChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        SubmissionTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Description",
            placeholder = "Brief description of the offer (optional)",
            helperText = "Add a description to help customers understand the offer better",
            focusRequester = focusRequester,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onNextStep() },
            ),
        )
    }
}

@Composable
private fun StartDateStepContent(
    startDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    PromocodeDatesStep(
        label = "Start Date",
        selectedDate = startDate,
        onDateSelected = onDateSelected,
        placeholder = "Select start date",
        isRequired = true,
    )
}

@Composable
private fun EndDateStepContent(
    endDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit
) {
    PromocodeDatesStep(
        label = "End Date",
        selectedDate = endDate,
        onDateSelected = { date -> onDateSelected(date) },
        placeholder = "Select end date",
        isRequired = true,
    )
}

@Preview(name = "Service Step Content", showBackground = true)
@Composable
private fun ServiceStepContentPreview() {
    QodeTheme {
        Column(modifier = Modifier.padding(SpacingTokens.md)) {
            SubmissionWizardStepContent(
                currentStep = PromocodeSubmissionStep.SERVICE,
                wizardData = SubmissionWizardData(),
                onAction = {},
            )
        }
    }
}

@Preview(name = "Promo Code Step Content", showBackground = true)
@Composable
private fun PromoCodeStepContentPreview() {
    QodeTheme {
        Column(modifier = Modifier.padding(SpacingTokens.md)) {
            SubmissionWizardStepContent(
                currentStep = PromocodeSubmissionStep.PROMOCODE,
                wizardData = SubmissionWizardData(promoCode = "SAVE20"),
                onAction = {},
            )
        }
    }
}
