package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeBorder
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.R
import network.chaintech.cmpcountrycodepicker.model.CountryDetails
import network.chaintech.cmpcountrycodepicker.ui.CountryPickerBasicTextField

/**
 * Phone input state for validation
 */
sealed class QodePhoneInputState {
    object Default : QodePhoneInputState()
    object Success : QodePhoneInputState()
    data class Error(val message: String) : QodePhoneInputState()
}

/**
 * Beautiful phone input component using CMPCountryCodePicker library
 * Fully integrates with Qode Material 3 theme system
 *
 * @param phoneNumber Current phone number value
 * @param onPhoneNumberChange Callback when phone number changes
 * @param modifier Modifier for styling
 * @param defaultCountryCode Default country code (ISO 2-letter code)
 * @param onCountrySelected Callback when country is selected
 * @param label Optional label text
 * @param placeholder Optional placeholder text
 * @param state Current validation state
 * @param enabled Whether input is enabled
 * @param readOnly Whether input is read-only
 * @param visualTransformation Visual transformation for input
 * @param onDone Callback when done button is pressed
 */
@Composable
fun QodePhoneInput(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    defaultCountryCode: String = "kz", // Kazakhstan by default
    onCountrySelected: (CountryDetails) -> Unit = {},
    label: String? = null,
    placeholder: String? = null,
    state: QodePhoneInputState = QodePhoneInputState.Default,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onDone: () -> Unit = {}
) {
    Column(modifier = modifier) {
        CountryPickerBasicTextField(
            mobileNumber = phoneNumber,
            defaultCountryCode = defaultCountryCode,
            onMobileNumberChange = onPhoneNumberChange,
            onCountrySelected = onCountrySelected,
            modifier = Modifier.fillMaxWidth(),
            defaultPaddingValues = PaddingValues(QodeSpacing.md),
            showCountryFlag = true,
            showCountryPhoneCode = true,
            showCountryName = false,
            showCountryCode = false,
            showArrowDropDown = true,
            spaceAfterCountryFlag = QodeSpacing.sm,
            spaceAfterCountryPhoneCode = QodeSpacing.sm,
            countryFlagSize = 24.dp,
            showVerticalDivider = true,
            spaceAfterVerticalDivider = QodeSpacing.sm,
            verticalDividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            verticalDividerHeight = 20.dp,
            countryPhoneCodeTextStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            label = label?.let { labelText ->
                {
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (state) {
                            is QodePhoneInputState.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            },
            placeholder = placeholder?.let { placeholderText ->
                {
                    Text(
                        text = placeholderText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            },
            supportingText = if (state is QodePhoneInputState.Error) {
                {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            } else {
                null
            },
            isError = state is QodePhoneInputState.Error,
            visualTransformation = visualTransformation,
            singleLine = true,
            shape = RoundedCornerShape(QodeCorners.md),
            colors = when (state) {
                is QodePhoneInputState.Error -> OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.error,
                    unfocusedBorderColor = MaterialTheme.colorScheme.error,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    focusedLabelColor = MaterialTheme.colorScheme.error,
                    unfocusedLabelColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    cursorColor = MaterialTheme.colorScheme.error,
                    errorCursorColor = MaterialTheme.colorScheme.error,
                )
                is QodePhoneInputState.Success -> OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.05f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                )
                else -> OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                )
            },
            focusedBorderThickness = QodeBorder.medium,
            unfocusedBorderThickness = QodeBorder.thin,
            onDone = onDone,
        )
    }
}

/**
 * Simplified phone input for quick usage with default strings
 */
@Composable
fun QodePhoneInputSimple(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {}
) {
    QodePhoneInput(
        phoneNumber = phoneNumber,
        onPhoneNumberChange = onPhoneNumberChange,
        modifier = modifier,
        defaultCountryCode = "kz", // Kazakhstan default
        onCountrySelected = { /* No-op for simple version */ },
        label = stringResource(R.string.phone_number),
        placeholder = stringResource(R.string.enter_phone_number),
        onDone = onDone,
    )
}

/**
 * Phone input with validation state management
 */
@Composable
fun QodePhoneInputWithValidation(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.phone_number),
    placeholder: String = stringResource(R.string.enter_phone_number),
    isRequired: Boolean = true,
    onValidationChange: (Boolean) -> Unit = {},
    onDone: () -> Unit = {}
) {
    // String resources for validation messages
    val requiredMessage = stringResource(R.string.validation_phone_required)
    val tooShortMessage = stringResource(R.string.validation_phone_too_short)
    val tooLongMessage = stringResource(R.string.validation_phone_too_long)

    // Simple validation logic using string resources
    val validationState = remember(phoneNumber) {
        when {
            phoneNumber.isEmpty() && isRequired -> QodePhoneInputState.Error(requiredMessage)
            phoneNumber.isNotEmpty() && phoneNumber.length < 7 -> QodePhoneInputState.Error(tooShortMessage)
            phoneNumber.length > 15 -> QodePhoneInputState.Error(tooLongMessage)
            phoneNumber.isNotEmpty() && phoneNumber.length >= 7 -> QodePhoneInputState.Success
            else -> QodePhoneInputState.Default
        }
    }

    // Fixed: Use LaunchedEffect for side effects instead of remember
    LaunchedEffect(validationState) {
        onValidationChange(validationState is QodePhoneInputState.Success)
    }

    QodePhoneInput(
        phoneNumber = phoneNumber,
        onPhoneNumberChange = onPhoneNumberChange,
        modifier = modifier,
        defaultCountryCode = "kz",
        onCountrySelected = { /* No-op, validation doesn't need country selection */ },
        label = label,
        placeholder = placeholder,
        state = validationState,
        onDone = onDone,
    )
}

// Previews
@Preview(name = "Phone Input States", showBackground = true)
@Composable
private fun QodePhoneInputPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.lg),
        ) {
            var phoneNumber1 by remember { mutableStateOf("") }
            QodePhoneInput(
                phoneNumber = phoneNumber1,
                onPhoneNumberChange = { phoneNumber1 = it },
                label = stringResource(R.string.phone_number),
                placeholder = stringResource(R.string.enter_phone_number),
                state = QodePhoneInputState.Default,
            )

            var phoneNumber2 by remember { mutableStateOf("7771234567") }
            QodePhoneInput(
                phoneNumber = phoneNumber2,
                onPhoneNumberChange = { phoneNumber2 = it },
                label = stringResource(R.string.phone_number),
                state = QodePhoneInputState.Success,
            )

            var phoneNumber3 by remember { mutableStateOf("123") }
            QodePhoneInput(
                phoneNumber = phoneNumber3,
                onPhoneNumberChange = { phoneNumber3 = it },
                label = stringResource(R.string.phone_number),
                state = QodePhoneInputState.Error(stringResource(R.string.invalid_phone_number)),
            )

            var phoneNumber4 by remember { mutableStateOf("7771234567") }
            QodePhoneInput(
                phoneNumber = phoneNumber4,
                onPhoneNumberChange = { phoneNumber4 = it },
                label = stringResource(R.string.phone_number),
                enabled = false,
            )
        }
    }
}

@Preview(name = "Simple Phone Input", showBackground = true)
@Composable
private fun QodePhoneInputSimplePreview() {
    QodeTheme {
        var phoneNumber by remember { mutableStateOf("") }

        QodePhoneInputSimple(
            phoneNumber = phoneNumber,
            onPhoneNumberChange = { phoneNumber = it },
            modifier = Modifier.padding(QodeSpacing.md),
        )
    }
}

@Preview(name = "Phone Input with Validation", showBackground = true)
@Composable
private fun QodePhoneInputWithValidationPreview() {
    QodeTheme {
        var phoneNumber by remember { mutableStateOf("") }
        var isValid by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
        ) {
            QodePhoneInputWithValidation(
                phoneNumber = phoneNumber,
                onPhoneNumberChange = { phoneNumber = it },
                onValidationChange = { isValid = it },
            )

            Text(
                text = "Valid: $isValid",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
