package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.qodein.feature.promocode.submission.ProgressiveStep
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.ServiceSelectionUiState
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.shared.model.Service
import kotlinx.coroutines.delay

@Composable
fun CurrentStepContent(
    currentStep: ProgressiveStep,
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
            ProgressiveStep.SERVICE -> ServiceStepContent(
                selectedService = wizardData.selectedService,
                serviceName = wizardData.serviceName,
                serviceSelectionUiState = serviceSelectionUiState,
                onShowServiceSelector = { onAction(SubmissionWizardAction.ShowServiceSelector) },
                onServiceNameChange = { onAction(SubmissionWizardAction.UpdateServiceName(it)) },
                onToggleManualEntry = { onAction(SubmissionWizardAction.ToggleManualEntry) },
                focusRequester = focusRequester,
            )

            ProgressiveStep.DISCOUNT_TYPE -> DiscountTypeStepContent(
                selectedType = wizardData.promoCodeType,
                onTypeSelected = { onAction(SubmissionWizardAction.UpdatePromoCodeType(it)) },
            )

            ProgressiveStep.PROMO_CODE -> PromoCodeStepContent(
                promoCode = wizardData.promoCode,
                onPromoCodeChange = { onAction(SubmissionWizardAction.UpdatePromoCode(it)) },
                focusRequester = focusRequester,
                keyboardController = keyboardController,
            )

            ProgressiveStep.DISCOUNT_VALUE -> DiscountValueStepContent(
                promoCodeType = wizardData.promoCodeType,
                discountPercentage = wizardData.discountPercentage,
                discountAmount = wizardData.discountAmount,
                minimumOrderAmount = wizardData.minimumOrderAmount,
                onDiscountPercentageChange = { onAction(SubmissionWizardAction.UpdateDiscountPercentage(it)) },
                onDiscountAmountChange = { onAction(SubmissionWizardAction.UpdateDiscountAmount(it)) },
                onMinimumOrderAmountChange = { onAction(SubmissionWizardAction.UpdateMinimumOrderAmount(it)) },
                focusRequester = focusRequester,
            )

            ProgressiveStep.OPTIONAL -> OptionsStepContent(
                isFirstUserOnly = wizardData.isFirstUserOnly,
                description = wizardData.description,
                onFirstUserOnlyChange = { onAction(SubmissionWizardAction.UpdateFirstUserOnly(it)) },
                onDescriptionChange = { onAction(SubmissionWizardAction.UpdateDescription(it)) },
                focusRequester = focusRequester,
            )

            ProgressiveStep.START_DATE -> StartDateStepContent(
                startDate = wizardData.startDate,
                onDateSelected = { onAction(SubmissionWizardAction.UpdateStartDate(it)) },
            )

            ProgressiveStep.END_DATE -> EndDateStepContent(
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
    var validationState by remember { mutableStateOf(ValidationState.IDLE) }

    // Simple validation logic
    LaunchedEffect(promoCode) {
        if (promoCode.isNotEmpty()) {
            validationState = ValidationState.VALIDATING
            delay(500) // Simulate validation
            validationState = if (promoCode.length >= 3) {
                ValidationState.VALID
            } else {
                ValidationState.ERROR
            }
        } else {
            validationState = ValidationState.IDLE
        }
    }

    SubmissionTextField(
        value = promoCode,
        onValueChange = { newValue ->
            // Format promo code: uppercase, no spaces, alphanumeric only
            val formatted = newValue.uppercase().filter { it.isLetterOrDigit() }.take(20)
            onPromoCodeChange(formatted)
        },
        label = "Promo Code",
        placeholder = "Enter promo code (e.g., SAVE20)",
        leadingIcon = QodeCommerceIcons.PromoCode,
        validationState = validationState,
        errorText = if (validationState == ValidationState.ERROR && promoCode.isNotEmpty()) {
            "Promo code must be at least 3 characters"
        } else {
            null
        },
        helperText = "Make it memorable and unique (3-20 characters)",
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
    minimumOrderAmount: String,
    onDiscountPercentageChange: (String) -> Unit,
    onDiscountAmountChange: (String) -> Unit,
    onMinimumOrderAmountChange: (String) -> Unit,
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
                                androidx.compose.material3.Text(
                                    text = "Customer saves $percentage% on their order",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
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

        // Minimum order amount
        SubmissionTextField(
            value = minimumOrderAmount,
            onValueChange = onMinimumOrderAmountChange,
            label = "Minimum Order Amount",
            placeholder = "1000",
            fieldType = SubmissionFieldType.CURRENCY,
            leadingIcon = QodeCommerceIcons.Dollar,
            helperText = "Minimum order value to apply this discount",
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number,
            ),
        )
    }
}

@Composable
private fun OptionsStepContent(
    isFirstUserOnly: Boolean,
    description: String,
    onFirstUserOnlyChange: (Boolean) -> Unit,
    onDescriptionChange: (String) -> Unit,
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

        SubmissionTextField(
            value = if (isFirstUserOnly) "first" else "all",
            onValueChange = { value ->
                onFirstUserOnlyChange(value == "first")
            },
            label = "Customer Eligibility",
            fieldType = SubmissionFieldType.DROPDOWN,
            options = options,
            helperText = "Choose who can use this promo code",
        )

        // Description
        SubmissionTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Description",
            placeholder = "Brief description of the offer (optional)",
            helperText = "Add a description to help customers understand the offer",
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
    SubmissionTextField(
        value = startDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")),
        onValueChange = { },
        label = "Start Date",
        placeholder = "Select start date",
        fieldType = SubmissionFieldType.DATE,
        onDateClick = {
            // TODO: Show date picker
            // For now, we'll use today's date
            onDateSelected(java.time.LocalDate.now())
        },
        helperText = "When should customers be able to start using this code?",
        isRequired = true,
    )
}

@Composable
private fun EndDateStepContent(
    endDate: java.time.LocalDate?,
    onDateSelected: (java.time.LocalDate?) -> Unit
) {
    SubmissionTextField(
        value = endDate?.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "",
        onValueChange = { },
        label = "End Date",
        placeholder = "Select end date (optional)",
        fieldType = SubmissionFieldType.DATE,
        onDateClick = {
            // TODO: Show date picker
            // For now, we'll use a date 30 days from now
            onDateSelected(java.time.LocalDate.now().plusDays(30))
        },
        helperText = "When should this promo code expire? Leave empty for no expiration",
    )
}

@Preview(name = "Service Step Content", showBackground = true)
@Composable
private fun ServiceStepContentPreview() {
    QodeTheme {
        Column(modifier = Modifier.padding(SpacingTokens.md)) {
            CurrentStepContent(
                currentStep = ProgressiveStep.SERVICE,
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
            CurrentStepContent(
                currentStep = ProgressiveStep.PROMO_CODE,
                wizardData = SubmissionWizardData(promoCode = "SAVE20"),
                serviceSelectionUiState = ServiceSelectionUiState.Default,
                onAction = {},
            )
        }
    }
}
