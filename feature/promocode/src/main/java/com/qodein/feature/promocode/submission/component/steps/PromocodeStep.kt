package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.PromocodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import com.qodein.shared.model.PromocodeCode

@Composable
internal fun PromocodeStep(
    promocode: String,
    onPromocodeChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit
) {
    QodeinTextField(
        value = promocode,
        onValueChange = { onPromocodeChange(it) },
        placeholder = stringResource(R.string.promo_code_step_placeholder),
        leadingIcon = PromocodeIcons.Promocode,
        helperText = stringResource(R.string.promo_code_step_helper_text),
        focusRequester = focusRequester,
        showPasteIcon = true,
        maxLength = PromocodeCode.MAX_LENGTH,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Characters,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNextStep() },
        ),
    )
}

@PreviewLightDark
@Composable
private fun FixedDiscountPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeWizardStep.PROMOCODE,
            wizardData = SubmissionWizardData(),
            onAction = {},
        )
    }
}
