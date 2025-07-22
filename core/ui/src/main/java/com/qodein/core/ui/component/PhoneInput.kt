package com.qodein.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSize
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.R
import com.simon.xmaterialccp.data.CountryData
import com.simon.xmaterialccp.data.utils.getLibCountries

@Composable
fun QodePhoneInput(
    phoneNumber: String,
    selectedCountry: CountryData?,
    validationState: PhoneValidationState,
    onPhoneNumberChange: (String) -> Unit,
    onCountryClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isReadOnly: Boolean = false,
    showValidation: Boolean = true,
    showClearButton: Boolean = true,
    placeholder: String = stringResource(R.string.phone_input_placeholder),
    contentDescription: String = stringResource(R.string.phone_input_content_desc)
) {
    Column(
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
        },
    ) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(
                            onClick = onCountryClick,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        )
                        .padding(horizontal = QodeSpacing.sm),
                ) {
                    Text(
                        text = getCountryFlag(selectedCountry?.countryCode ?: "KZ"),
                        modifier = Modifier.padding(horizontal = QodeSpacing.xs),
                    )
                    Text(
                        text = selectedCountry?.countryPhoneCode ?: "+7",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = QodeSpacing.xs),
                    )
                    Icon(
                        imageVector = QodeActionIcons.Down,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            trailingIcon = {
                if (showClearButton && phoneNumber.isNotEmpty()) {
                    IconButton(
                        onClick = { onPhoneNumberChange("") },
                        modifier = Modifier.size(QodeSize.iconSmall),
                    ) {
                        Icon(
                            imageVector = QodeActionIcons.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            isError = validationState is PhoneValidationState.Error,
            enabled = isEnabled,
            readOnly = isReadOnly,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(QodeCorners.md),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        // Validation indicator
        if (showValidation && validationState !is PhoneValidationState.Idle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = QodeSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(QodeSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (validationState) {
                    is PhoneValidationState.Error -> {
                        Icon(
                            imageVector = QodeNavigationIcons.Error,
                            contentDescription = null,
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
                            contentDescription = null,
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
    }
}

private fun getCountryFlag(countryCode: String): String =
    when (countryCode) {
        "KZ" -> "🇰🇿"
        "US" -> "🇺🇸"
        "RU" -> "🇷🇺"
        "GB" -> "🇬🇧"
        "DE" -> "🇩🇪"
        "FR" -> "🇫🇷"
        else -> "🏳️"
    }

@Composable
private fun getErrorMessage(error: PhoneValidationError): String =
    when (error) {
        PhoneValidationError.TOO_SHORT -> stringResource(R.string.phone_input_too_short)
        PhoneValidationError.TOO_LONG -> stringResource(R.string.phone_input_too_long)
        PhoneValidationError.INVALID_FORMAT -> stringResource(R.string.phone_number_invalid_format)
        PhoneValidationError.REQUIRED -> stringResource(R.string.phone_input_required)
    }

private fun getDefaultCountry(): CountryData? =
    try {
        getLibCountries().find { it.countryCode == "KZ" }
    } catch (_: Exception) {
        CountryData(cCodes = "KZ", countryPhoneCode = "+7", cNames = "Kazakhstan", flagResID = 0)
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
        PhoneInputPreviewData("Idle", "777123456", PhoneValidationState.Idle),
        PhoneInputPreviewData("Valid", "777123456", PhoneValidationState.Valid("+7 777 123 45 67")),
        PhoneInputPreviewData("Error", "123", PhoneValidationState.Error(PhoneValidationError.TOO_SHORT)),
        PhoneInputPreviewData("Validating", "777123456", PhoneValidationState.Validating),
    )
}

data class PhoneInputPreviewData(val name: String, val phoneNumber: String, val validationState: PhoneValidationState)

@Preview(name = "Phone Input", showBackground = true)
@Composable
private fun QodePhoneInputPreview(@PreviewParameter(PhoneInputPreviewProvider::class) previewData: PhoneInputPreviewData) {
    QodeTheme {
        Surface(modifier = Modifier.padding(QodeSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(QodeSpacing.md)) {
                Text(
                    text = "Phone Input - ${previewData.name}",
                    style = MaterialTheme.typography.titleMedium,
                )
                QodePhoneInput(
                    phoneNumber = previewData.phoneNumber,
                    selectedCountry = getDefaultCountry(),
                    validationState = previewData.validationState,
                    onPhoneNumberChange = {},
                    onCountryClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
