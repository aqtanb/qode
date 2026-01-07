package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.PromocodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.validation.ValidationState
import com.qodein.feature.promocode.submission.validation.getBusinessLogicValidationError
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep

@Composable
internal fun DiscountValueStep(
    promoCodeType: PromocodeType?,
    discountPercentage: String,
    discountAmount: String,
    minimumOrderAmount: String,
    wizardData: SubmissionWizardData,
    onDiscountPercentageChange: (String) -> Unit,
    onDiscountAmountChange: (String) -> Unit,
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

            else -> {
                DiscountAmountField(
                    value = discountAmount,
                    onValueChange = onDiscountAmountChange,
                    focusRequester = focusRequester,
                    onMoveToNext = { minimumOrderFocusRequester.requestFocus() },
                )
            }
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
    QodeinTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = stringResource(R.string.promocode_discount_percentage_placeholder),
        leadingIcon = QodeIcons.Sale,
        helperText = stringResource(R.string.promocode_discount_percentage_helper),
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onMoveToNext() },
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
    QodeinTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = stringResource(R.string.promocode_discount_amount_placeholder),
        leadingIcon = QodeIcons.Dollar,
        helperText = stringResource(R.string.promocode_discount_amount_helper),
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onMoveToNext() },
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

    QodeinTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = stringResource(R.string.promocode_minimum_order_placeholder),
        leadingIcon = QodeIcons.Dollar,
        helperText = stringResource(R.string.promocode_minimum_order_helper),
        errorText = getBusinessLogicValidationError(wizardData),
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
            currentStep = PromocodeSubmissionStep.DISCOUNT_VALUE,
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
            currentStep = PromocodeSubmissionStep.DISCOUNT_VALUE,
            wizardData = SubmissionWizardData(promocodeType = PromocodeType.PERCENTAGE),
            validation = ValidationState.valid(),
            onAction = {},
        )
    }
}
