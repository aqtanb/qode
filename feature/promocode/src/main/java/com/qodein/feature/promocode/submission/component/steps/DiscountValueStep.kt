package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.feature.promocode.submission.PromocodeSubmissionStep
import com.qodein.feature.promocode.submission.PromocodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.ValidationState
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard

@Composable
internal fun DiscountValueStep(
    promoCodeType: PromocodeType?,
    discountPercentage: String,
    discountAmount: String,
    onDiscountPercentageChange: (String) -> Unit,
    onDiscountAmountChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit
) {
    when (promoCodeType) {
        PromocodeType.PERCENTAGE -> {
            QodeinTextField(
                value = discountPercentage,
                onValueChange = onDiscountPercentageChange,
                placeholder = "30",
                leadingIcon = QodeIcons.Sale,
                helperText = "Enter the percentage (1-99%)",
                focusRequester = focusRequester,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Companion.Next,
                    keyboardType = KeyboardType.Companion.Number,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { onNextStep() },
                ),
            )
        }

        else -> {
            QodeinTextField(
                value = discountAmount,
                onValueChange = onDiscountAmountChange,
                placeholder = "2000",
                leadingIcon = QodeIcons.Dollar,
                helperText = "Enter in ₸ (tenge)",
                focusRequester = focusRequester,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Companion.Next,
                    keyboardType = KeyboardType.Companion.Number,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { onNextStep() },
                ),
            )
        }
    }
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
