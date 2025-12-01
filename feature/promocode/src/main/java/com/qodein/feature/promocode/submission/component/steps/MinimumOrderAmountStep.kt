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
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.ValidationState
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.validation.getBusinessLogicValidationError

@Composable
internal fun MinimumOrderAmountStep(
    minimumOrderAmount: String,
    onMinimumOrderAmountChange: (String) -> Unit,
    focusRequester: FocusRequester,
    wizardData: SubmissionWizardData,
    onNextStep: () -> Unit
) {
    QodeinTextField(
        value = minimumOrderAmount,
        onValueChange = onMinimumOrderAmountChange,
        placeholder = "5000",
        leadingIcon = QodeIcons.Dollar,
        helperText = "Minimum order in ₸ (tenge) to get the discount",
        errorText = getBusinessLogicValidationError(wizardData),
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

@ThemePreviews
@Composable
private fun MinimalOrderAmountStepPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeSubmissionStep.MINIMUM_ORDER,
            wizardData = SubmissionWizardData(),
            validation = ValidationState.valid(),
            onAction = {},
        )
    }
}
