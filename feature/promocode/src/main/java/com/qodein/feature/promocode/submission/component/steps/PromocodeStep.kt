package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.feature.promocode.submission.PromocodeSubmissionStep
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.FieldValidationState
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.component.SubmissionTextField
import com.qodein.feature.promocode.submission.validation.getPromoCodeValidationError

@Composable
internal fun PromocodeStep(
    promoCode: String,
    onPromoCodeChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit
) {
    var validationState by remember { mutableStateOf(FieldValidationState.IDLE) }

    LaunchedEffect(promoCode) {
        validationState = if (promoCode.isNotEmpty()) {
            if (getPromoCodeValidationError(promoCode) == null) {
                FieldValidationState.VALID
            } else {
                FieldValidationState.ERROR
            }
        } else {
            FieldValidationState.IDLE
        }
    }

    QodeinTextField(
        value = promoCode,
        onValueChange = { newValue ->
            // Smart formatting: default uppercase but allow user control
            val formatted = newValue
                .filter { it.isLetterOrDigit() || it == '-' || it == ' ' }
                .take(50) // Allow up to 50 chars to handle most real promo codes
            onPromoCodeChange(formatted)
        },
        placeholder = "Enter promo code (e.g., SAVE20)",
        leadingIcon = QodeIcons.Promocode,
        errorText = if (validationState == FieldValidationState.ERROR && promoCode.isNotEmpty()) {
            getPromoCodeValidationError(promoCode)
        } else {
            null
        },
        helperText = "Enter the promo code exactly as shown (2-50 characters)",
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Companion.Characters,
            imeAction = ImeAction.Companion.Next,
            keyboardType = KeyboardType.Companion.Text,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNextStep() },
        ),
    )
}

@ThemePreviews
@Composable
private fun PromocodeStepPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeSubmissionStep.PROMOCODE,
            wizardData = SubmissionWizardData(),
            onAction = {},
        )
    }
}
