package com.qodein.core.ui.component

import android.R.attr.contentDescription
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.simon.xmaterialccp.data.CountryData
import com.simon.xmaterialccp.data.utils.checkPhoneNumber
import com.simon.xmaterialccp.data.utils.getDefaultLangCode
import com.simon.xmaterialccp.data.utils.getFlags
import com.simon.xmaterialccp.data.utils.getLibCountries
import com.simon.xmaterialccp.transformation.PhoneNumberTransformation

@Composable
fun QodePhoneInput(
    phoneNumber: String,
    selectedCountry: CountryData,
    validationState: PhoneValidationState,
    onPhoneNumberChange: (String) -> Unit,
    onCountryPickerClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = validationState is PhoneValidationState.Error,
    label: String = stringResource(R.string.phone_input_label),
    placeholder: String = stringResource(R.string.phone_input_placeholder)
) {
    val fullPhoneNumber by remember(phoneNumber, selectedCountry) {
        derivedStateOf { "${selectedCountry.countryPhoneCode}$phoneNumber" }
    }

    // Use the library's phone number formatter
    val phoneFormatter = remember(selectedCountry) {
        PhoneNumberTransformation(selectedCountry.countryCode.uppercase())
    }

    Column(
        modifier = modifier.semantics {
            this.contentDescription = "Phone number input"
        },
    ) {
        // Phone input field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { newValue ->
                // Filter digits only and apply reasonable length limit
                val digitsOnly = newValue.filter { it.isDigit() }
                if (digitsOnly.length <= 15) { // International standard max length
                    onPhoneNumberChange(digitsOnly)
                }
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                CountrySelector(
                    country = selectedCountry,
                    onClick = onCountryPickerClick,
                    enabled = enabled,
                )
            },
            trailingIcon = {
                if (phoneNumber.isNotEmpty() && enabled) {
                    IconButton(
                        onClick = { onPhoneNumberChange("") },
                    ) {
                        Icon(
                            imageVector = QodeActionIcons.Close,
                            contentDescription = "Clear phone number",
                            modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            isError = isError,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = phoneFormatter,
            singleLine = true,
            shape = RoundedCornerShape(ShapeTokens.Corner.medium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(SizeTokens.TextField.height),
        )

        // Validation indicator
        ValidationIndicator(
            validationState = validationState,
            modifier = Modifier.padding(top = SpacingTokens.xs),
        )
    }
}

@Composable
private fun CountrySelector(
    country: CountryData,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(
                onClick = onClick,
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(horizontal = SpacingTokens.sm),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        // Country flag
        Image(
            painter = painterResource(id = getFlags(country.countryCode)),
            contentDescription = stringResource(
                R.string.phone_input_flag_content_desc,
                country.cNames,
            ),
            modifier = Modifier.size(24.dp),
        )

        // Phone code
        Text(
            text = country.countryPhoneCode,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
        )

        // Dropdown indicator
        Icon(
            imageVector = QodeActionIcons.Down,
            contentDescription = if (enabled) {
                stringResource(R.string.phone_input_dropdown_content_desc)
            } else {
                null
            },
            modifier = Modifier.size(16.dp),
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            },
        )
    }
}

@Composable
private fun ValidationIndicator(
    validationState: PhoneValidationState,
    modifier: Modifier = Modifier
) {
    if (validationState is PhoneValidationState.Idle) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (validationState) {
            is PhoneValidationState.Error -> {
                Icon(
                    imageVector = QodeNavigationIcons.Error,
                    contentDescription = stringResource(R.string.phone_input_error_content_desc),
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = getErrorMessage(validationState.error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            is PhoneValidationState.Valid -> {
                Icon(
                    imageVector = QodeStatusIcons.Verified,
                    contentDescription = stringResource(R.string.phone_input_success_content_desc),
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.phone_input_valid),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            is PhoneValidationState.Validating -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.phone_input_validating),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun getErrorMessage(error: PhoneValidationError): String =
    when (error) {
        PhoneValidationError.TOO_SHORT -> stringResource(R.string.phone_input_too_short)
        PhoneValidationError.TOO_LONG -> stringResource(R.string.phone_input_too_long)
        PhoneValidationError.INVALID_FORMAT -> stringResource(R.string.phone_number_invalid_format)
        PhoneValidationError.REQUIRED -> stringResource(R.string.phone_input_required)
    }

// Validation logic using the library's function
fun validatePhoneNumber(
    phoneNumber: String,
    country: CountryData
): PhoneValidationState =
    when {
        phoneNumber.isEmpty() -> PhoneValidationState.Error(PhoneValidationError.REQUIRED)
        phoneNumber.length < 6 -> PhoneValidationState.Error(PhoneValidationError.TOO_SHORT)
        phoneNumber.length > 15 -> PhoneValidationState.Error(PhoneValidationError.TOO_LONG)
        else -> {
            val fullNumber = "${country.countryPhoneCode}$phoneNumber"
            val isValid = checkPhoneNumber(phoneNumber, fullNumber, country.countryCode)
            if (isValid) {
                PhoneValidationState.Valid(fullNumber)
            } else {
                PhoneValidationState.Error(PhoneValidationError.INVALID_FORMAT)
            }
        }
    }

sealed interface PhoneValidationState {
    object Idle : PhoneValidationState
    object Validating : PhoneValidationState
    data class Valid(val fullPhoneNumber: String) : PhoneValidationState
    data class Error(val error: PhoneValidationError) : PhoneValidationState
}

enum class PhoneValidationError {
    TOO_SHORT,
    TOO_LONG,
    INVALID_FORMAT,
    REQUIRED
}

// Preview stuff
class PhoneInputPreviewProvider : PreviewParameterProvider<PhoneInputPreviewData> {
    override val values = sequenceOf(
        PhoneInputPreviewData("Idle", "7771234567", PhoneValidationState.Idle),
        PhoneInputPreviewData("Valid", "7771234567", PhoneValidationState.Valid("+7 777 123 45 67")),
        PhoneInputPreviewData("Error", "123", PhoneValidationState.Error(PhoneValidationError.TOO_SHORT)),
        PhoneInputPreviewData("Validating", "777123456", PhoneValidationState.Validating),
    )
}

data class PhoneInputPreviewData(val name: String, val phoneNumber: String, val validationState: PhoneValidationState)

@Preview(name = "Phone Input", showBackground = true)
@Composable
private fun QodePhoneInputPreview(@PreviewParameter(PhoneInputPreviewProvider::class) previewData: PhoneInputPreviewData) {
    val context = LocalContext.current
    val defaultCountry = remember {
        try {
            getLibCountries().single { it.countryCode == getDefaultLangCode(context) }
        } catch (_: Exception) {
            getLibCountries().find { it.countryCode == "kz" } ?: getLibCountries().first()
        }
    }

    QodeTheme {
        Surface(modifier = Modifier.padding(SpacingTokens.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)) {
                Text(
                    text = "Phone Input - ${previewData.name}",
                    style = MaterialTheme.typography.titleMedium,
                )
                QodePhoneInput(
                    phoneNumber = previewData.phoneNumber,
                    selectedCountry = defaultCountry,
                    validationState = previewData.validationState,
                    onPhoneNumberChange = {},
                    onCountryPickerClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
