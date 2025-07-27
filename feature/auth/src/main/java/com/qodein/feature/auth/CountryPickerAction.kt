package com.qodein.feature.auth

import com.qodein.core.model.Country

sealed interface CountryPickerAction {
    data object LoadCountries : CountryPickerAction
    data class SearchCountries(val query: String) : CountryPickerAction
    data class SelectCountry(val country: Country) : CountryPickerAction
    data object ToggleSearch : CountryPickerAction
    data object ClearSearch : CountryPickerAction
    data object NavigateBack : CountryPickerAction
}
