package com.qodein.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import com.simon.xmaterialccp.data.utils.getDefaultLangCode
import com.simon.xmaterialccp.data.utils.getLibCountries
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(@ApplicationContext private val context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        updateDefaultCountry()
    }

    private fun updateDefaultCountry() {
        val localeCountryCode = getDefaultLangCode(context)
        val smartDefault = getLibCountries().find {
            it.countryCode == localeCountryCode
        } ?: _uiState.value.selectedCountry

        _uiState.value = _uiState.value.copy(
            selectedCountry = smartDefault,
        )
    }
    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.PhoneNumberChanged -> {
                _uiState.value = _uiState.value.copy(
                    phoneNumber = action.phoneNumber,
                )
            }
            is AuthAction.CountrySelected -> {
                _uiState.value = _uiState.value.copy(
                    selectedCountry = action.selectedCountry,
                )
            }
            is AuthAction.SignInWithGoogleClicked -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                )
            }
        }
    }
}
