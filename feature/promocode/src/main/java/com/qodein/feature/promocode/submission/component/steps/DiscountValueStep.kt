package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.PromocodeIcons
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.PromocodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.validation.ValidationState
import com.qodein.feature.promocode.submission.validation.getBusinessLogicValidationError
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode

@Composable
internal fun DiscountValueStep(
    promoCodeType: PromocodeType?,
    discountPercentage: String,
    discountAmount: String,
    freeItemDescription: String,
    minimumOrderAmount: String,
    wizardData: SubmissionWizardData,
    onDiscountPercentageChange: (String) -> Unit,
    onDiscountAmountChange: (String) -> Unit,
    onFreeItemDescriptionChange: (String) -> Unit,
    onMinimumOrderAmountChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit
) {
    val minimumOrderFocusRequester = remember { FocusRequester() }

    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xl),
    ) {
        when (promoCodeType) {
            PromocodeType.PERCENTAGE -> {
                DiscountPercentageField(
                    value = discountPercentage,
                    onValueChange = onDiscountPercentageChange,
                    focusRequester = focusRequester,
                    onMoveToNext = { minimumOrderFocusRequester.requestFocus() },
                )
            }

            PromocodeType.FIXED_AMOUNT -> {
                DiscountAmountField(
                    value = discountAmount,
                    onValueChange = onDiscountAmountChange,
                    focusRequester = focusRequester,
                    onMoveToNext = { minimumOrderFocusRequester.requestFocus() },
                )
            }

            PromocodeType.FREE_ITEM -> {
                FreeItemDescriptionField(
                    value = freeItemDescription,
                    onValueChange = onFreeItemDescriptionChange,
                    focusRequester = focusRequester,
                    onMoveToNext = { minimumOrderFocusRequester.requestFocus() },
                )
            }

            null -> { }
        }

        MinimumOrderField(
            value = minimumOrderAmount,
            onValueChange = onMinimumOrderAmountChange,
            wizardData = wizardData,
            focusRequester = minimumOrderFocusRequester,
            onSubmitForm = onNextStep,
        )
    }
}

@Composable
private fun DiscountPercentageField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onMoveToNext: () -> Unit
) {
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    val blankErrorText = stringResource(R.string.promocode_discount_percentage_error_blank)
    val invalidRangeText = stringResource(R.string.promocode_discount_percentage_error_range)

    QodeinTextField(
        value = value,
        onValueChange = { newValue ->
            val numValue = newValue.toDoubleOrNull()
            if (numValue == null || numValue <= 100) {
                errorText = null
                onValueChange(newValue)
            } else {
                errorText = invalidRangeText
            }
        },
        placeholder = stringResource(R.string.promocode_discount_percentage_placeholder),
        leadingIcon = QodeIcons.Sale,
        helperText = stringResource(R.string.promocode_discount_percentage_helper),
        errorText = errorText,
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                if (value.isBlank()) {
                    errorText = blankErrorText
                } else {
                    val numValue = value.toDoubleOrNull()
                    if (numValue == null || numValue <= 0 || numValue > 100) {
                        errorText = invalidRangeText
                    } else {
                        errorText = null
                        onMoveToNext()
                    }
                }
            },
        ),
    )
}

@Composable
private fun DiscountAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onMoveToNext: () -> Unit
) {
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    val blankErrorText = stringResource(R.string.promocode_discount_amount_error_blank)

    QodeinTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = stringResource(R.string.promocode_discount_amount_placeholder),
        leadingIcon = QodeIcons.Dollar,
        helperText = stringResource(R.string.promocode_discount_amount_helper),
        errorText = errorText,
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                if (value.isBlank()) {
                    errorText = blankErrorText
                } else {
                    errorText = null
                    onMoveToNext()
                }
            },
        ),
    )
}

@Composable
private fun FreeItemDescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onMoveToNext: () -> Unit
) {
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    val blankErrorText = stringResource(R.string.promocode_free_item_error_blank)
    val maxLengthErrorText = stringResource(
        R.string.promocode_free_item_error_max_length,
        Discount.FreeItem.MAX_DESCRIPTION_LENGTH,
    )

    QodeinTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isLetterOrDigit() || it == ' ' || it == '-' }
            val clamped = filtered.take(Discount.FreeItem.MAX_DESCRIPTION_LENGTH)
            errorText = when {
                filtered.length >= Discount.FreeItem.MAX_DESCRIPTION_LENGTH -> maxLengthErrorText
                else -> null
            }
            onValueChange(clamped)
        },
        placeholder = stringResource(R.string.promocode_free_item_placeholder),
        leadingIcon = PromocodeIcons.FreeItem,
        helperText = stringResource(R.string.promocode_free_item_helper),
        errorText = errorText,
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Sentences,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                if (value.isBlank()) {
                    errorText = blankErrorText
                } else {
                    errorText = null
                    onMoveToNext()
                }
            },
        ),
    )
}

@Composable
private fun MinimumOrderField(
    value: String,
    onValueChange: (String) -> Unit,
    wizardData: SubmissionWizardData,
    focusRequester: FocusRequester,
    onSubmitForm: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    val maxLengthErrorText = stringResource(
        R.string.promocode_minimum_order_error_max_length,
        Promocode.MINIMUM_ORDER_AMOUNT_MAX_LENGTH,
    )
    val businessLogicError = getBusinessLogicValidationError(wizardData)

    QodeinTextField(
        value = value,
        onValueChange = { newValue ->
            val clamped = newValue.take(Promocode.MINIMUM_ORDER_AMOUNT_MAX_LENGTH)
            errorText = when {
                newValue.length >= Promocode.MINIMUM_ORDER_AMOUNT_MAX_LENGTH -> maxLengthErrorText
                else -> null
            }
            onValueChange(clamped)
        },
        placeholder = stringResource(R.string.promocode_minimum_order_placeholder),
        leadingIcon = QodeIcons.Dollar,
        helperText = stringResource(R.string.promocode_minimum_order_helper),
        errorText = errorText ?: businessLogicError,
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            imeAction = if (value.isEmpty()) ImeAction.Previous else ImeAction.Next,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                keyboardController?.hide()
                onSubmitForm()
            },
        ),
    )
}

@ThemePreviews
@Composable
private fun FixedDiscountPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeWizardStep.DISCOUNT_VALUE,
            wizardData = SubmissionWizardData(promocodeType = PromocodeType.FIXED_AMOUNT),
            validation = ValidationState.valid(),
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun PercentageDiscountPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeWizardStep.DISCOUNT_VALUE,
            wizardData = SubmissionWizardData(promocodeType = PromocodeType.PERCENTAGE),
            validation = ValidationState.valid(),
            onAction = {},
        )
    }
}
