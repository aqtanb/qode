package com.qodein.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.R

/**
 * Simple country data for Kazakhstan-focused app
 */
data class QodeCountry(val code: String, val name: String, val phoneCode: String, val flag: String)

/**
 * Pre-defined countries for Kazakhstan market
 */
private val qodeCountries = listOf(
    QodeCountry("KZ", "Kazakhstan", "+7", "🇰🇿"),
    QodeCountry("RU", "Russia", "+7", "🇷🇺"),
    QodeCountry("US", "United States", "+1", "🇺🇸"),
    QodeCountry("GB", "United Kingdom", "+44", "🇬🇧"),
    QodeCountry("DE", "Germany", "+49", "🇩🇪"),
    QodeCountry("FR", "France", "+33", "🇫🇷"),
    QodeCountry("CN", "China", "+86", "🇨🇳"),
    QodeCountry("IN", "India", "+91", "🇮🇳"),
    QodeCountry("TR", "Turkey", "+90", "🇹🇷"),
    QodeCountry("UZ", "Uzbekistan", "+998", "🇺🇿"),
    QodeCountry("KG", "Kyrgyzstan", "+996", "🇰🇬"),
    QodeCountry("TJ", "Tajikistan", "+992", "🇹🇯"),
    QodeCountry("TM", "Turkmenistan", "+993", "🇹🇲"),
)

/**
 * 🇰🇿 Simple, reliable phone input for Kazakhstan
 * No crashes, works everywhere!
 */
@Composable
fun QodePhoneInputSimple(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onCountrySelected: (QodeCountry) -> Unit = {},
    defaultCountryCode: String = "KZ",
    label: String = stringResource(R.string.phone_number),
    placeholder: String = stringResource(R.string.enter_phone_number),
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCountry by remember {
        mutableStateOf(
            qodeCountries.find { it.code == defaultCountryCode } ?: qodeCountries.first(),
        )
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            isError = isError,
            supportingText = if (isError && errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else {
                null
            },
            leadingIcon = {
                Box {
                    Row(
                        modifier = Modifier
                            .clickable { expanded = true }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = selectedCountry.flag,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = selectedCountry.phoneCode,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select country",
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        qodeCountries.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = country.flag,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            text = "${country.name} ${country.phoneCode}",
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCountry = country
                                    expanded = false
                                    onCountrySelected(country)
                                },
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
        )
    }
}

/**
 * Phone input with validation
 */
@Composable
fun QodePhoneInputWithValidation(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onValidationChange: (Boolean) -> Unit = {},
    onCountrySelected: (QodeCountry) -> Unit = {}
) {
    val (isError, errorMessage) = remember(phoneNumber) {
        when {
            phoneNumber.isEmpty() -> false to null
            phoneNumber.length < 6 -> true to "Phone number too short"
            phoneNumber.length > 15 -> true to "Phone number too long"
            else -> false to null
        }
    }

    remember(isError) {
        onValidationChange(!isError && phoneNumber.isNotEmpty())
        null
    }

    QodePhoneInputSimple(
        phoneNumber = phoneNumber,
        onPhoneNumberChange = onPhoneNumberChange,
        modifier = modifier,
        onCountrySelected = onCountrySelected,
        isError = isError,
        errorMessage = errorMessage,
    )
}

@Preview(name = "Simple Phone Input", showBackground = true)
@Composable
private fun QodePhoneInputSimplePreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            var phoneNumber1 by remember { mutableStateOf("") }
            QodePhoneInputSimple(
                phoneNumber = phoneNumber1,
                onPhoneNumberChange = { phoneNumber1 = it },
            )

            var phoneNumber2 by remember { mutableStateOf("123") }
            QodePhoneInputSimple(
                phoneNumber = phoneNumber2,
                onPhoneNumberChange = { phoneNumber2 = it },
                isError = true,
                errorMessage = "Too short",
            )

            var phoneNumber3 by remember { mutableStateOf("") }
            QodePhoneInputWithValidation(
                phoneNumber = phoneNumber3,
                onPhoneNumberChange = { phoneNumber3 = it },
            )
        }
    }
}
