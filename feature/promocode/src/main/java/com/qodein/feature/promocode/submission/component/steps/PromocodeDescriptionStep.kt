package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.qodein.core.designsystem.component.QodeinTextField

@Composable
internal fun PromocodeDescriptionStep(
    description: String,
    onDescriptionChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit
) {
    QodeinTextField(
        value = description,
        onValueChange = onDescriptionChange,
        placeholder = "Brief description of the offer (optional)",
        helperText = "Add a description to help customers understand the offer better",
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Companion.Done,
            keyboardType = KeyboardType.Companion.Text,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onNextStep() },
        ),
    )
}
