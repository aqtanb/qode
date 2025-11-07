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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.PromocodeSubmissionAction
import com.qodein.feature.promocode.submission.PromocodeSubmissionStep
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.steps.DateSelector
import com.qodein.feature.promocode.submission.component.steps.PromocodeTypeSelector
import com.qodein.feature.promocode.submission.component.steps.ServiceStepContent
import com.qodein.feature.promocode.submission.validation.getBusinessLogicValidationError
import com.qodein.feature.promocode.submission.validation.getPromoCodeValidationError
import kotlinx.coroutines.delay
import java.time.LocalDate

// Simple UI validation states for field feedback
private enum class FieldValidationState {
    IDLE,
    VALIDATING,
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
            PromocodeSubmissionStep.SERVICE -> ServiceStepContent(
                selectedService = wizardData.selectedService,
                serviceName = wizardData.serviceName,
                isManualEntry = wizardData.isManualServiceEntry,
                onShowServiceSelector = { onAction(PromocodeSubmissionAction.ShowServiceSelector) },
                onServiceNameChange = { onAction(PromocodeSubmissionAction.UpdateServiceName(it)) },
                onToggleManualEntry = { onAction(PromocodeSubmissionAction.ToggleManualEntry) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeSubmissionStep.DISCOUNT_TYPE -> DiscountTypeStepContent(
                selectedType = wizardData.promoCodeType,
                onTypeSelected = { onAction(PromocodeSubmissionAction.UpdatePromoCodeType(it)) },
            )

            PromocodeSubmissionStep.PROMO_CODE -> PromoCodeStepContent(
                promoCode = wizardData.promoCode,
                onPromoCodeChange = { onAction(PromocodeSubmissionAction.UpdatePromoCode(it)) },
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeSubmissionStep.DISCOUNT_VALUE -> DiscountValueStepContent(
                promoCodeType = wizardData.promoCodeType,
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
private fun DiscountTypeStepContent(
    selectedType: PromoCodeType?,
    onTypeSelected: (PromoCodeType) -> Unit
) {
    PromocodeTypeSelector(
        selectedType = selectedType,
        onTypeSelected = onTypeSelected,
    )
}

// TODO: Add paste button
@Composable
private fun PromoCodeStepContent(
    promoCode: String,
    onPromoCodeChange: (String) -> Unit,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    onNextStep: () -> Unit
) {
    var validationState by remember { mutableStateOf(FieldValidationState.IDLE) }

    // Centralized validation for promo codes
    LaunchedEffect(promoCode) {
        if (promoCode.isNotEmpty()) {
            validationState = FieldValidationState.VALIDATING
            delay(300) // Simulate validation

            validationState = if (getPromoCodeValidationError(promoCode) == null) {
                FieldValidationState.VALID
            } else {
                FieldValidationState.ERROR
            }
        } else {
            validationState = FieldValidationState.IDLE
        }
    }

    SubmissionTextField(
        value = promoCode,
        onValueChange = { newValue ->
            // Smart formatting: default uppercase but allow user control
            val formatted = newValue
                .filter { it.isLetterOrDigit() || it == '-' || it == ' ' }
                .take(50) // Allow up to 50 chars to handle most real promo codes
            onPromoCodeChange(formatted)
        },
        label = "Promo Code",
        placeholder = "Enter promo code (e.g., SAVE20)",
        leadingIcon = QodeCommerceIcons.PromoCode,
        errorText = if (validationState == FieldValidationState.ERROR && promoCode.isNotEmpty()) {
            getPromoCodeValidationError(promoCode)
        } else {
            null
        },
        helperText = "Enter the promo code exactly as shown (2-50 characters)",
        isRequired = true,
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Companion.Characters,
            imeAction = ImeAction.Companion.Next,
            keyboardType = KeyboardType.Companion.Text,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNextStep() },
        ),
    )
}

@Composable
private fun DiscountValueStepContent(
    promoCodeType: PromoCodeType?,
    discountPercentage: String,
    discountAmount: String,
    onDiscountPercentageChange: (String) -> Unit,
    onDiscountAmountChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Discount value field (changes based on type)
        when (promoCodeType) {
            PromoCodeType.PERCENTAGE -> {
                SubmissionTextField(
                    value = discountPercentage,
                    onValueChange = onDiscountPercentageChange,
                    label = "Discount Percentage",
                    placeholder = "20",
                    fieldType = SubmissionFieldType.PERCENTAGE,
                    leadingIcon = QodeCommerceIcons.Sale,
                    helperText = "Enter percentage (1-99%)",
                    isRequired = true,
                    focusRequester = focusRequester,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onNextStep() },
                    ),
                    supportingContent = {
                        if (discountPercentage.isNotEmpty()) {
                            val percentage = discountPercentage.toIntOrNull() ?: 0
                            if (percentage > 0) {
                                Text(
                                    text = "Customer saves $percentage% on their order",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    },
                )
            }
            PromoCodeType.FIXED_AMOUNT -> {
                SubmissionTextField(
                    value = discountAmount,
                    onValueChange = onDiscountAmountChange,
                    label = "Discount Amount",
                    placeholder = "500",
                    fieldType = SubmissionFieldType.CURRENCY,
                    leadingIcon = QodeCommerceIcons.Dollar,
                    helperText = "Enter amount in ₸ (tenge)",
                    isRequired = true,
                    focusRequester = focusRequester,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onNextStep() },
                    ),
                    supportingContent = {
                        if (discountAmount.isNotEmpty()) {
                            val amount = discountAmount.toIntOrNull() ?: 0
                            if (amount > 0) {
                                Text(
                                    text = "Customer saves ₸$amount on their order",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    },
                )
            }
            null -> {
                SubmissionTextField(
                    value = "",
                    onValueChange = { },
                    label = "Discount Value",
                    placeholder = "Select discount type first",
                    enabled = false,
                    helperText = "Choose a discount type in the previous step",
                )
            }
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
            leadingIcon = QodeCommerceIcons.Dollar,
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
    DateSelector(
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
    DateSelector(
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
                currentStep = PromocodeSubmissionStep.PROMO_CODE,
                wizardData = SubmissionWizardData(promoCode = "SAVE20"),
                onAction = {},
            )
        }
    }
}
