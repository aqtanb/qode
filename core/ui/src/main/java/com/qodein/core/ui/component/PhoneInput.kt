package com.qodein.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSize
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.ui.R
import com.simon.xmaterialccp.data.CountryData
import com.simon.xmaterialccp.data.utils.getDefaultLangCode
import com.simon.xmaterialccp.data.utils.getFlags
import com.simon.xmaterialccp.data.utils.getLibCountries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QodePhoneInput(
    phoneNumber: String,
    selectedCountry: CountryData?,
    validationState: PhoneValidationState,
    onPhoneNumberChange: (String) -> Unit,
    onCountryClick: (CountryData) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isReadOnly: Boolean = false,
    showValidation: Boolean = true,
    showClearButton: Boolean = true,
    placeholder: String = stringResource(R.string.phone_input_placeholder),
    contentDescription: String = stringResource(R.string.phone_input_content_desc)
) {
    val context = LocalContext.current
    var showCountryPicker by remember { mutableStateOf(false) }

    // Use XMaterial CCP's default country logic
    val defaultCountry = remember {
        try {
            getLibCountries().single { it.countryCode == getDefaultLangCode(context) }
        } catch (_: Exception) {
            getLibCountries().find { it.countryCode == "KZ" } ?: getLibCountries().first()
        }
    }

    val currentCountry = selectedCountry ?: defaultCountry

    Column(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription },
    ) {
        // Phone input field with your beautiful design
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { newValue ->
                // Filter digits only and limit appropriately
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
            leadingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(
                            onClick = { showCountryPicker = true },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        )
                        .padding(horizontal = QodeSpacing.sm),
                ) {
                    // Use XMaterial CCP's flag system
                    Image(
                        painter = painterResource(id = getFlags(currentCountry.countryCode)),
                        contentDescription = currentCountry.cNames,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = QodeSpacing.xs),
                    )
                    Text(
                        text = currentCountry.countryPhoneCode,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = QodeSpacing.xs),
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
                            contentDescription = stringResource(R.string.close),
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
            modifier = Modifier.fillMaxWidth().height(SizeTokens.TextField.height),
        )

        // Full-width country picker dialog
        if (showCountryPicker) {
            val configuration = LocalConfiguration.current
            BasicAlertDialog(
                onDismissRequest = { showCountryPicker = false },
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = QodeSpacing.xxl),
                    shape = RoundedCornerShape(QodeCorners.lg),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                ) {
                    CountryPickerDialog(
                        onCountrySelected = { country ->
                            onCountryClick(country) // This will update the selectedCountry in parent
                            showCountryPicker = false
                        },
                        onDismiss = { showCountryPicker = false },
                        selectedCountry = currentCountry,
                    )
                }
            }
        }

        // Your clean validation indicator
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

@Composable
private fun CountryPickerDialog(
    onCountrySelected: (CountryData) -> Unit,
    onDismiss: () -> Unit,
    selectedCountry: CountryData?
) {
    var searchQuery by remember { mutableStateOf("") }
    val countries = remember { getLibCountries() }

    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            countries
        } else {
            countries.filter { country ->
                country.cNames.contains(searchQuery, ignoreCase = true) ||
                    country.countryPhoneCode.contains(searchQuery) ||
                    country.countryCode.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(QodeSpacing.lg),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.select_country),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = QodeActionIcons.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(text = stringResource(R.string.search_countries))
            },
            leadingIcon = {
                Icon(
                    imageVector = QodeNavigationIcons.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(QodeCorners.md),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = QodeSpacing.md),
        )

        // Countries list - full width
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(filteredCountries) { country ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCountrySelected(country) }
                        .padding(QodeSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Use XMaterial CCP's flag system
                    Image(
                        painter = painterResource(id = getFlags(country.countryCode)),
                        contentDescription = country.cNames,
                        modifier = Modifier
                            .size(QodeSize.iconLarge)
                            .padding(end = QodeSpacing.md),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = country.cNames,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = country.countryPhoneCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Show selection indicator
                    if (selectedCountry?.countryCode == country.countryCode) {
                        Icon(
                            imageVector = QodeStatusIcons.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
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
            getLibCountries().find { it.countryCode == "KZ" } ?: getLibCountries().first()
        }
    }

    QodeTheme {
        Surface(modifier = Modifier.padding(QodeSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(QodeSpacing.md)) {
                Text(
                    text = "Phone Input - ${previewData.name}",
                    style = MaterialTheme.typography.titleMedium,
                )
                QodePhoneInput(
                    phoneNumber = previewData.phoneNumber,
                    selectedCountry = defaultCountry,
                    validationState = previewData.validationState,
                    onPhoneNumberChange = {},
                    onCountryClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
