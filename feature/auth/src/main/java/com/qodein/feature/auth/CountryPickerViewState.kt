package com.qodein.feature.auth

import com.qodein.core.model.Country

data class CountryPickerViewState(
    val countries: List<Country> = emptyList(),
    val filteredCountries: List<Country> = emptyList(),
    val selectedCountry: Country? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
