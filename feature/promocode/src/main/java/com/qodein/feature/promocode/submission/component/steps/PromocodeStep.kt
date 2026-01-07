package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep
import com.qodein.shared.model.PromocodeCode

@Composable
internal fun PromocodeStep(
    promocode: String,
    onPromocodeChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit
) {
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    val blankErrorText = stringResource(R.string.promo_code_step_error_blank)
    val maxLengthErrorText = stringResource(
        R.string.promo_code_step_error_max_length,
        PromocodeCode.MAX_LENGTH,
    )

    QodeinTextField(
        value = promocode,
        onValueChange = { newValue ->
            val sanitized = newValue
                .uppercase()
                .filter { it.isLetterOrDigit() || it in setOf('-', '_') }
                .take(PromocodeCode.MAX_LENGTH)

            errorText = when {
                sanitized.length >= PromocodeCode.MAX_LENGTH -> maxLengthErrorText
                else -> null
            }

            onPromocodeChange(sanitized)
        },
        placeholder = stringResource(R.string.promo_code_step_placeholder),
        leadingIcon = QodeIcons.Promocode,
        errorText = errorText,
        helperText = stringResource(R.string.promo_code_step_helper_text),
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Text,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                when {
                    promocode.isBlank() -> {
                        errorText = blankErrorText
                    }
                    else -> {
                        errorText = null
                        onNextStep()
                    }
                }
            },
        ),
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
