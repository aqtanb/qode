package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.SubmissionFieldType
import com.qodein.feature.promocode.submission.component.SubmissionTextField
import com.qodein.feature.promocode.submission.validation.getBusinessLogicValidationError

@Composable
internal fun MinimumOrderAmountStep(
    minimumOrderAmount: String,
    onMinimumOrderAmountChange: (String) -> Unit,
    focusRequester: FocusRequester,
    wizardData: SubmissionWizardData,
    onNextStep: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        SubmissionTextField(
            value = minimumOrderAmount,
            onValueChange = onMinimumOrderAmountChange,
            label = "Minimum Order Amount",
            placeholder = "1000",
            fieldType = SubmissionFieldType.CURRENCY,
            leadingIcon = QodeIcons.Dollar,
            helperText = "Minimum order value required to apply this discount",
            errorText = getBusinessLogicValidationError(wizardData),
            isRequired = true,
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
