package com.qodein.feature.auth

import com.simon.xmaterialccp.data.CountryData
import com.simon.xmaterialccp.data.utils.getLibCountries

private val defaultCountry = getLibCountries().find { it.countryCode == "kz" }
    ?: getLibCountries().first()

data class AuthUiState(val phoneNumber: String = "", val selectedCountry: CountryData = defaultCountry, val isLoading: Boolean = false)
