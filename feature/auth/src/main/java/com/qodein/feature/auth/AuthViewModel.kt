package com.qodein.feature.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.domain.usecase.GetCountriesUseCase
import com.qodein.core.domain.usecase.GetDefaultCountryUseCase
import com.qodein.core.model.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getDefaultCountryUseCase: GetDefaultCountryUseCase,
    private val getCountriesUseCase: GetCountriesUseCase // ✅ ADD THIS
) : ViewModel() {

    private val _state = MutableStateFlow(AuthViewState())
    val state = _state.asStateFlow()

    init {
        loadDefaultCountry()
        observeCountryResult()
    }

    fun handleAction(action: AuthAction) {
        when (action) {
            is AuthAction.LoadInitialData -> loadDefaultCountry()
            is AuthAction.PhoneNumberChanged -> updatePhoneNumber(action.phoneNumber)
            is AuthAction.CountrySelected -> updateSelectedCountry(action.selectedCountry)
            is AuthAction.SendVerificationCodeClicked -> sendVerificationCode()
            is AuthAction.SignInWithGoogleClicked -> signInWithGoogle()
            is AuthAction.OpenCountryPicker -> { /* Navigation handled in UI */ }
        }
    }

    private fun loadDefaultCountry() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val defaultCountry = getDefaultCountryUseCase()
                _state.value = _state.value.copy(
                    selectedCountry = defaultCountry,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load default country",
                )
            }
        }
    }

    private fun observeCountryResult() {
        // ✅ FIXED - Observe our own SavedStateHandle
        savedStateHandle.getStateFlow<String?>("selected_country_code", null)
            .onEach { countryCode ->
                countryCode?.let {
                    viewModelScope.launch {
                        try {
                            val countries = getCountriesUseCase()
                            val country = countries.find { it.code == countryCode }
                            country?.let {
                                updateSelectedCountry(it)
                                // Clear after processing
                                savedStateHandle.remove<String>("selected_country_code")
                            }
                        } catch (e: Exception) {
                            _state.value = _state.value.copy(error = "Failed to load selected country")
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updatePhoneNumber(phoneNumber: String) {
        _state.value = _state.value.copy(phoneNumber = phoneNumber)
    }

    private fun updateSelectedCountry(country: Country) {
        _state.value = _state.value.copy(selectedCountry = country)
    }

    private fun sendVerificationCode() {
        val currentState = _state.value
        if (!currentState.isPhoneNumberValid) return

        _state.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // TODO: Implement with AuthRepository
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to send verification code",
                )
            }
        }
    }

    private fun signInWithGoogle() {
        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // TODO: Implement with AuthRepository
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to sign in with Google",
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
