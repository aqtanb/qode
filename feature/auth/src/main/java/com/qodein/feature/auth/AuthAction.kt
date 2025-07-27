package com.qodein.feature.auth

import com.simon.xmaterialccp.data.CountryData

sealed interface AuthAction {
    data class PhoneNumberChanged(val phoneNumber: String) : AuthAction
    data object SignInWithGoogleClicked : AuthAction
    data class CountrySelected(val selectedCountry: CountryData) : AuthAction
}
