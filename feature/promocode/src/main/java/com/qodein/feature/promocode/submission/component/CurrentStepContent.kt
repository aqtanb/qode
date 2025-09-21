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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.ServiceSelectionUiState
import com.qodein.feature.promocode.submission.SubmissionStep
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.shared.model.Service
import kotlinx.coroutines.delay

// Simple UI validation states for field feedback
private enum class FieldValidationState {
    IDLE,
    VALIDATING,
    VALID,
    ERROR
}

@Composable
fun SubmissionWizardStepContent(
    currentStep: SubmissionStep,
    wizardData: SubmissionWizardData,
    serviceSelectionUiState: ServiceSelectionUiState,
    onAction: (SubmissionWizardAction) -> Unit,
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
            SubmissionStep.SERVICE -> ServiceStepContent(
                selectedService = wizardData.selectedService,
                serviceName = wizardData.serviceName,
                serviceSelectionUiState = serviceSelectionUiState,
                onShowServiceSelector = { onAction(SubmissionWizardAction.ShowServiceSelector) },
                onServiceNameChange = { onAction(SubmissionWizardAction.UpdateServiceName(it)) },
                onToggleManualEntry = { onAction(SubmissionWizardAction.ToggleManualEntry) },
                focusRequester = focusRequester,
            )

            SubmissionStep.DISCOUNT_TYPE -> DiscountTypeStepContent(
                selectedType = wizardData.promoCodeType,
                onTypeSelected = { onAction(SubmissionWizardAction.UpdatePromoCodeType(it)) },
            )

            SubmissionStep.PROMO_CODE -> PromoCodeStepContent(
                promoCode = wizardData.promoCode,
                onPromoCodeChange = { onAction(SubmissionWizardAction.UpdatePromoCode(it)) },
                focusRequester = focusRequester,
                keyboardController = keyboardController,
            )

            SubmissionStep.DISCOUNT_VALUE -> DiscountValueStepContent(
                promoCodeType = wizardData.promoCodeType,
                discountPercentage = wizardData.discountPercentage,
                discountAmount = wizardData.discountAmount,
                onDiscountPercentageChange = { onAction(SubmissionWizardAction.UpdateDiscountPercentage(it)) },
                onDiscountAmountChange = { onAction(SubmissionWizardAction.UpdateDiscountAmount(it)) },
                focusRequester = focusRequester,
            )

            SubmissionStep.MINIMUM_ORDER -> MinimumOrderStepContent(
                minimumOrderAmount = wizardData.minimumOrderAmount,
                onMinimumOrderAmountChange = { onAction(SubmissionWizardAction.UpdateMinimumOrderAmount(it)) },
                focusRequester = focusRequester,
            )

            SubmissionStep.ELIGIBILITY -> EligibilityStepContent(
                isFirstUserOnly = wizardData.isFirstUserOnly,
                isOneTimeUseOnly = wizardData.isOneTimeUseOnly,
                onFirstUserOnlyChange = { onAction(SubmissionWizardAction.UpdateFirstUserOnly(it)) },
                onOneTimeUseOnlyChange = { onAction(SubmissionWizardAction.UpdateOneTimeUseOnly(it)) },
                focusRequester = focusRequester,
            )

            SubmissionStep.DESCRIPTION -> DescriptionStepContent(
                description = wizardData.description,
                onDescriptionChange = { onAction(SubmissionWizardAction.UpdateDescription(it)) },
                focusRequester = focusRequester,
            )

            SubmissionStep.START_DATE -> StartDateStepContent(
                startDate = wizardData.startDate,
                onDateSelected = { onAction(SubmissionWizardAction.UpdateStartDate(it)) },
            )

            SubmissionStep.END_DATE -> EndDateStepContent(
                endDate = wizardData.endDate,
                onDateSelected = { date ->
                    if (date != null) {
                        onAction(SubmissionWizardAction.UpdateEndDate(date))
                    }
                },
            )
        }
    }
}

@Composable
private fun ServiceStepContent(
    selectedService: Service?,
    serviceName: String,
    serviceSelectionUiState: ServiceSelectionUiState,
    onShowServiceSelector: () -> Unit,
    onServiceNameChange: (String) -> Unit,
    onToggleManualEntry: () -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        when (serviceSelectionUiState) {
            ServiceSelectionUiState.ManualEntry -> {
                // Manual entry mode - show text field with option to browse
                SubmissionTextField(
                    value = serviceName,
                    onValueChange = onServiceNameChange,
                    label = "Service Name",
                    placeholder = "Type the service name",
                    leadingIcon = QodeCommerceIcons.Store,
                    helperText = "Exact service name",
                    focusRequester = focusRequester,
                    isRequired = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text,
                    ),
                )

                // Secondary action to browse services - center aligned for better UX
                Text(
                    text = "Browse services instead",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SpacingTokens.sm)
                        .clickable { onToggleManualEntry() },
                )
            }
            else -> {
                // Default mode - show service selector with manual as secondary
                ServiceSelector(
                    selectedService = selectedService,
                    placeholder = "Search for the service",
                    onServiceSelectorClick = onShowServiceSelector,
                )

                // Secondary action for manual entry - using Column for better text flow
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SpacingTokens.sm),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Can't find the service?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Type manually",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = SpacingTokens.xs)
                            .clickable { onToggleManualEntry() },
                    )
                }
            }
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

@Composable
private fun PromoCodeStepContent(
    promoCode: String,
    onPromoCodeChange: (String) -> Unit,
    focusRequester: FocusRequester,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    var validationState by remember { mutableStateOf(FieldValidationState.IDLE) }

    // Realistic validation for existing promo codes
    LaunchedEffect(promoCode) {
        if (promoCode.isNotEmpty()) {
            validationState = FieldValidationState.VALIDATING
            delay(300) // Simulate validation

            // Clean promo code (remove spaces, hyphens for validation)
            val cleanCode = promoCode.replace("[\\s-]".toRegex(), "")

            validationState = when {
                cleanCode.length < 2 -> FieldValidationState.ERROR
                cleanCode.length > 50 -> FieldValidationState.ERROR
                !cleanCode.matches("[A-Za-z0-9]+".toRegex()) -> FieldValidationState.ERROR
                else -> FieldValidationState.VALID
            }
        } else {
            validationState = FieldValidationState.IDLE
        }
    }

    SubmissionTextField(
        value = promoCode,
        onValueChange = { newValue ->
            // Format promo code: allow realistic formats, limit length
            val formatted = newValue.uppercase()
                .filter { it.isLetterOrDigit() || it == '-' || it == ' ' }
                .take(50) // Allow up to 50 chars to handle most real promo codes
            onPromoCodeChange(formatted)
        },
        label = "Promo Code",
        placeholder = "Enter promo code (e.g., SAVE20)",
        leadingIcon = QodeCommerceIcons.PromoCode,
        errorText = if (validationState == FieldValidationState.ERROR && promoCode.isNotEmpty()) {
            val cleanCode = promoCode.replace("[\\s-]".toRegex(), "")
            when {
                cleanCode.length < 2 -> "Promo code is too short"
                cleanCode.length > 50 -> "Promo code is too long"
                !cleanCode.matches("[A-Za-z0-9]+".toRegex()) -> "Use only letters and numbers"
                else -> "Invalid promo code format"
            }
        } else {
            null
        },
        helperText = "Enter the promo code exactly as shown (2-50 characters)",
        isRequired = true,
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Text,
        ),
        keyboardActions = KeyboardActions(
            onNext = { keyboardController?.hide() },
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
    focusRequester: FocusRequester
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
                    supportingContent = {
                        if (discountAmount.isNotEmpty()) {
                            val amount = discountAmount.toIntOrNull() ?: 0
                            if (amount > 0) {
                                androidx.compose.material3.Text(
                                    text = "Customer saves ₸$amount on their order",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
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
    focusRequester: FocusRequester
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
            isRequired = true,
            focusRequester = focusRequester,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number,
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
    focusRequester: FocusRequester
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
        )
    }
}

@Composable
private fun StartDateStepContent(
    startDate: java.time.LocalDate,
    onDateSelected: (java.time.LocalDate) -> Unit
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
    endDate: java.time.LocalDate?,
    onDateSelected: (java.time.LocalDate?) -> Unit
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
                currentStep = SubmissionStep.SERVICE,
                wizardData = SubmissionWizardData(),
                serviceSelectionUiState = ServiceSelectionUiState.Default,
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
                currentStep = SubmissionStep.PROMO_CODE,
                wizardData = SubmissionWizardData(promoCode = "SAVE20"),
                serviceSelectionUiState = ServiceSelectionUiState.Default,
                onAction = {},
            )
        }
    }
}
