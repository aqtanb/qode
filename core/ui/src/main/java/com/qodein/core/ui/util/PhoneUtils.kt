package com.qodein.core.ui.util

import android.content.Context
import com.qodein.core.ui.component.PhoneValidationError
import com.qodein.core.ui.component.PhoneValidationState
import com.simon.xmaterialccp.data.CountryData
import com.simon.xmaterialccp.data.utils.checkPhoneNumber
import com.simon.xmaterialccp.data.utils.getDefaultLangCode
import com.simon.xmaterialccp.data.utils.getLibCountries
import com.simon.xmaterialccp.utils.searchCountry

/**
 * Utility class for phone number operations using XMaterialCCP library
 * Following the existing project patterns
 */
object PhoneUtils {

    /**
     * Get all available countries from the library
     */
    fun getAllCountries(): List<CountryData> = getLibCountries()

    /**
     * Get default country based on device locale/network
     * Follows the pattern from your existing code
     */
    fun getDefaultCountry(context: Context): CountryData {
        val defaultCountryCode = getDefaultLangCode(context)
        return getAllCountries().find { it.countryCode == defaultCountryCode }
            ?: getAllCountries().find { it.countryCode == "kz" } // Fallback to Kazakhstan
            ?: getAllCountries().first() // Ultimate fallback
    }

    /**
     * Validate phone number using the library's validation
     * Returns our internal validation result type
     */
    fun validatePhoneNumber(
        phoneNumber: String,
        country: CountryData
    ): PhoneValidationResult =
        when {
            phoneNumber.isEmpty() -> PhoneValidationResult.Empty
            phoneNumber.length < 6 -> PhoneValidationResult.TooShort
            phoneNumber.length > 15 -> PhoneValidationResult.TooLong
            else -> {
                val fullNumber = "${country.countryPhoneCode}$phoneNumber"
                val isValid = checkPhoneNumber(phoneNumber, fullNumber, country.countryCode)
                if (isValid) {
                    PhoneValidationResult.Valid(fullNumber)
                } else {
                    PhoneValidationResult.InvalidFormat
                }
            }
        }

    /**
     * Get country by country code
     */
    fun getCountryByCode(countryCode: String): CountryData? = getAllCountries().find { it.countryCode == countryCode }

    /**
     * Search countries by query using the library's search function
     */
    fun searchCountries(
        query: String,
        context: Context
    ): List<CountryData> {
        if (query.isBlank()) return getAllCountries()

        return getAllCountries().searchCountry(query, context)
    }

    /**
     * Format phone number with country code
     */
    fun formatFullPhoneNumber(
        phoneNumber: String,
        country: CountryData
    ): String = "${country.countryPhoneCode}$phoneNumber"
}

/**
 * Phone validation result sealed interface
 * Internal result type that gets converted to UI state
 */
sealed interface PhoneValidationResult {
    object Empty : PhoneValidationResult
    object TooShort : PhoneValidationResult
    object TooLong : PhoneValidationResult
    object InvalidFormat : PhoneValidationResult
    data class Valid(val fullPhoneNumber: String) : PhoneValidationResult
}

/**
 * Extension function to convert PhoneValidationResult to PhoneValidationState
 * Bridges between utility layer and UI layer
 */
fun PhoneValidationResult.toValidationState(): PhoneValidationState =
    when (this) {
        is PhoneValidationResult.Empty ->
            PhoneValidationState.Error(PhoneValidationError.REQUIRED)
        is PhoneValidationResult.TooShort ->
            PhoneValidationState.Error(PhoneValidationError.TOO_SHORT)
        is PhoneValidationResult.TooLong ->
            PhoneValidationState.Error(PhoneValidationError.TOO_LONG)
        is PhoneValidationResult.InvalidFormat ->
            PhoneValidationState.Error(PhoneValidationError.INVALID_FORMAT)
        is PhoneValidationResult.Valid ->
            PhoneValidationState.Valid(fullPhoneNumber)
    }
