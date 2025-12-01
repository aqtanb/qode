package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.error.asUiText
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.PromocodeSubmissionStep
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.ValidationState
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.model.PromocodeCode

@Composable
internal fun PromocodeStep(
    promocode: String,
    onPromocodeChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit,
    promocodeError: PromocodeError.CreationFailure? = null
) {
    QodeinTextField(
        value = promocode,
        onValueChange = { newValue ->
            onPromocodeChange(newValue)
        },
        placeholder = stringResource(R.string.promo_code_step_placeholder),
        leadingIcon = QodeIcons.Promocode,
        errorText = promocodeError?.asUiText(),
        helperText = stringResource(R.string.promo_code_step_helper_text),
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Companion.Characters,
            imeAction = ImeAction.Companion.Next,
            keyboardType = KeyboardType.Companion.Text,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNextStep() },
        ),
        maxLength = PromocodeCode.MAX_LENGTH,
    )
}

@ThemePreviews
@Composable
private fun FixedDiscountPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeSubmissionStep.PROMOCODE,
            wizardData = SubmissionWizardData(),
            validation = ValidationState.valid(),
            onAction = {},
        )
    }
}
