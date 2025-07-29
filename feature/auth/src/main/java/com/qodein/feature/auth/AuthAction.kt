package com.qodein.feature.auth

import com.qodein.core.model.Country

sealed interface AuthAction {
    data object LoadInitialData : AuthAction
    data class PhoneNumberChanged(val phoneNumber: String) : AuthAction
    data class CountrySelected(val selectedCountry: Country) : AuthAction
    data object OpenCountryPicker : AuthAction
    data object SendVerificationCodeClicked : AuthAction
    data object SignInWithGoogleClicked : AuthAction
}
