package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.R
import com.simon.xmaterialccp.component.MaterialCountryCodePicker
import com.simon.xmaterialccp.data.ccpDefaultColors
import com.simon.xmaterialccp.data.utils.checkPhoneNumber
import com.simon.xmaterialccp.data.utils.getDefaultLangCode
import com.simon.xmaterialccp.data.utils.getDefaultPhoneCode
import com.simon.xmaterialccp.data.utils.getLibCountries
import com.simon.xmaterialccp.data.utils.setLocale

/**
 * 🇰🇿 Qode Country Code Picker Demo
 * Preview-safe implementation with proper error handling
 */
@Composable
fun CountryCodePickerDemo(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(QodeSpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.lg),
        ) {
            // Header
            Text(
                text = stringResource(R.string.ccp_demo_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            // Basic Example
            BasicCountryCodePickerExample()
        }
    }
}

/**
 * Safe country selection helper
 */
@Composable
private fun getSafeDefaultCountry(preferredCode: String) =
    remember(preferredCode) {
        val countries = getLibCountries()

        // Try to find preferred country (KZ for Kazakhstan)
        countries.find { it.countryCode == preferredCode }
            // Fallback to US if KZ not found
            ?: countries.find { it.countryCode == "US" }
            // Ultimate fallback to first country in list
            ?: countries.firstOrNull()
            // If somehow no countries exist, create a dummy one
            ?: run {
                // This should never happen, but just in case
                throw IllegalStateException("No countries available in library")
            }
    }

/**
 * Preview-safe country code picker
 */
@Composable
private fun BasicCountryCodePickerExample() {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    // Safe defaults for preview mode
    var phoneCode by remember {
        mutableStateOf(
            if (isPreview) {
                "+7"
            } else {
                try {
                    getDefaultPhoneCode(context)
                } catch (e: Exception) {
                    "+7"
                }
            },
        )
    }

    val phoneNumber = rememberSaveable { mutableStateOf("") }

    var defaultLang by rememberSaveable {
        mutableStateOf(
            if (isPreview) {
                "KZ"
            } else {
                try {
                    getDefaultLangCode(context)
                } catch (e: Exception) {
                    "KZ"
                }
            },
        )
    }

    var isValidPhone by remember { mutableStateOf(true) }

    // Get safe default country
    val defaultCountry = getSafeDefaultCountry(defaultLang)

    // Only set locale in real app, not preview
    LaunchedEffect(Unit) {
        if (!isPreview) {
            try {
                setLocale(context, "en")
            } catch (e: Exception) {
                // Ignore locale setting errors in preview
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.phone_number_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            MaterialCountryCodePicker(
                pickedCountry = { country ->
                    phoneCode = country.countryPhoneCode
                    defaultLang = country.countryCode
                },
                defaultCountry = defaultCountry, // ← Safe country selection
                error = !isValidPhone,
                text = phoneNumber.value,
                onValueChange = { newValue ->
                    phoneNumber.value = newValue
                    if (!isValidPhone) isValidPhone = true
                },

                // Typography matching Qode design system
                searchFieldPlaceHolderTextStyle = MaterialTheme.typography.bodyMedium,
                searchFieldTextStyle = MaterialTheme.typography.bodyMedium,
                phonenumbertextstyle = MaterialTheme.typography.bodyMedium,
                countrytextstyle = MaterialTheme.typography.bodyMedium,
                countrycodetextstyle = MaterialTheme.typography.bodyMedium,
                appbartitleStyle = MaterialTheme.typography.titleLarge,
                errorTextStyle = MaterialTheme.typography.bodySmall,

                // UI Configuration
                showErrorText = true,
                showCountryCodeInDIalog = true,
                showDropDownAfterFlag = true,
                showCountryFlag = true,
                showCountryCode = true,
                showClearIcon = true,
                showErrorIcon = true,
                isEnabled = true,

                // Shape configuration
                textFieldShapeCornerRadiusInPercentage = 25,
                searchFieldShapeCornerRadiusInPercentage = 25,
                countryItemBgShape = RoundedCornerShape(8.dp),
                flagShape = RoundedCornerShape(6.dp),

                // Colors matching Qode theme
                colors = ccpDefaultColors(
                    primaryColor = MaterialTheme.colorScheme.primary,
                    errorColor = MaterialTheme.colorScheme.error,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    surfaceColor = MaterialTheme.colorScheme.surface,
                    outlineColor = MaterialTheme.colorScheme.outline,
                    disabledOutlineColor = MaterialTheme.colorScheme.outline.copy(0.1f),
                    unfocusedOutlineColor = MaterialTheme.colorScheme.onBackground.copy(0.3f),
                    textColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    topAppBarColor = MaterialTheme.colorScheme.surface,
                    countryItemBgColor = MaterialTheme.colorScheme.surface,
                    searchFieldBgColor = MaterialTheme.colorScheme.surface,
                    dialogNavIconColor = MaterialTheme.colorScheme.onBackground,
                    dropDownIconTint = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                ),

                errorModifier = Modifier.padding(top = 4.dp, start = QodeSpacing.sm),
            )

            // Validation button
            val fullPhoneNumber = "$phoneCode${phoneNumber.value}"
            Button(
                onClick = {
                    if (!isPreview) {
                        try {
                            isValidPhone = checkPhoneNumber(
                                phone = phoneNumber.value,
                                fullPhoneNumber = fullPhoneNumber,
                                countryCode = defaultLang,
                            )
                        } catch (e: Exception) {
                            // Handle validation errors gracefully
                            isValidPhone = phoneNumber.value.isNotEmpty() && phoneNumber.value.length >= 6
                        }
                    } else {
                        // Simple validation for preview
                        isValidPhone = phoneNumber.value.length >= 6
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.phone_verify_button),
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            // Show debug info only if we have phone number
            if (phoneNumber.value.isNotEmpty()) {
                OutlinedButton(
                    onClick = { /* Debug info */ },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Debug: $fullPhoneNumber (${defaultCountry.countryCode})",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

/**
 * 🎯 Production-ready component with preview safety
 */
@Composable
fun QodePhoneInput(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onCountryChange: (countryCode: String, phoneCode: String) -> Unit,
    isError: Boolean = false,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var phoneCode by remember {
        mutableStateOf(
            if (isPreview) {
                "+7"
            } else {
                try {
                    getDefaultPhoneCode(context)
                } catch (e: Exception) {
                    "+7"
                }
            },
        )
    }

    var countryCode by remember {
        mutableStateOf(
            if (isPreview) {
                "KZ"
            } else {
                try {
                    getDefaultLangCode(context)
                } catch (e: Exception) {
                    "KZ"
                }
            },
        )
    }

    // Safe country selection
    val defaultCountry = getSafeDefaultCountry(countryCode)

    MaterialCountryCodePicker(
        pickedCountry = { country ->
            phoneCode = country.countryPhoneCode
            countryCode = country.countryCode
            onCountryChange(countryCode, phoneCode)
        },
        defaultCountry = defaultCountry, // ← Safe country selection
        error = isError,
        text = phoneNumber,
        onValueChange = onPhoneNumberChange,
        isEnabled = isEnabled,

        // Simplified for production use
        showCountryFlag = true,
        showCountryCode = true,
        showDropDownAfterFlag = true,
        textFieldShapeCornerRadiusInPercentage = 20,
        flagShape = RoundedCornerShape(6.dp),

        colors = ccpDefaultColors(
            primaryColor = MaterialTheme.colorScheme.primary,
            errorColor = MaterialTheme.colorScheme.error,
            backgroundColor = MaterialTheme.colorScheme.background,
            surfaceColor = MaterialTheme.colorScheme.surface,
            outlineColor = MaterialTheme.colorScheme.outline,
            textColor = MaterialTheme.colorScheme.onBackground,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),

        modifier = modifier,
    )
}

@Preview(
    name = "Country Code Picker Demo - Light",
    showBackground = true,
)
@Composable
private fun CountryCodePickerDemoPreview() {
    QodeTheme(darkTheme = false) {
        CountryCodePickerDemo()
    }
}

@Preview(
    name = "Country Code Picker Demo - Dark",
    showBackground = true,
)
@Composable
private fun CountryCodePickerDemoDarkPreview() {
    QodeTheme(darkTheme = true) {
        CountryCodePickerDemo()
    }
}

@Preview(
    name = "Qode Phone Input",
    showBackground = true,
)
@Composable
private fun QodePhoneInputPreview() {
    QodeTheme {
        QodePhoneInput(
            phoneNumber = "7771234567",
            onPhoneNumberChange = {},
            onCountryChange = { _, _ -> },
        )
    }
}
