package com.qodein.feature.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.domain.usecase.GetCountriesUseCase
import com.qodein.core.domain.usecase.GetDefaultCountryUseCase
import com.qodein.core.domain.usecase.SearchCountriesUseCase
import com.qodein.core.model.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountryPickerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCountriesUseCase: GetCountriesUseCase,
    private val getDefaultCountryUseCase: GetDefaultCountryUseCase,
    private val searchCountriesUseCase: SearchCountriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CountryPickerUiState())
    val state = _state.asStateFlow()

    init {
        loadCountries()
    }

    fun handleAction(action: CountryPickerAction) {
        when (action) {
            is CountryPickerAction.LoadCountries -> loadCountries()
            is CountryPickerAction.SearchCountries -> searchCountries(action.query)
            is CountryPickerAction.SelectCountry -> selectCountry(action.country)
            is CountryPickerAction.ToggleSearch -> toggleSearch()
            is CountryPickerAction.ClearSearch -> clearSearch()
            is CountryPickerAction.NavigateBack -> { /* Navigation handled in UI */ }
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val countries = getCountriesUseCase()
                val defaultCountry = getDefaultCountryUseCase()

                _state.value = _state.value.copy(
                    countries = countries,
                    filteredCountries = countries,
                    selectedCountry = defaultCountry,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load countries",
                )
            }
        }
    }

    private fun searchCountries(query: String) {
        _state.value = _state.value.copy(searchQuery = query)

        viewModelScope.launch {
            try {
                val filteredCountries = searchCountriesUseCase(query)
                _state.value = _state.value.copy(filteredCountries = filteredCountries)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Search failed")
            }
        }
    }

    private fun selectCountry(country: Country) {
        // Only update local state - navigation result handled in UI
        _state.value = _state.value.copy(selectedCountry = country)
    }

    private fun toggleSearch() {
        _state.value = _state.value.copy(isSearchActive = true)
    }

    private fun clearSearch() {
        _state.value = _state.value.copy(
            searchQuery = "",
            isSearchActive = false,
            filteredCountries = _state.value.countries,
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
