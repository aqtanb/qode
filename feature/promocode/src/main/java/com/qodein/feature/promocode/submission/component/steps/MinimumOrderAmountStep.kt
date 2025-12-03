package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.validation.ValidationState
import com.qodein.feature.promocode.submission.validation.getBusinessLogicValidationError
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep

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
        placeholder = stringResource(R.string.promocode_minimum_order_placeholder),
        leadingIcon = QodeIcons.Dollar,
        helperText = stringResource(R.string.promocode_minimum_order_helper),
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
