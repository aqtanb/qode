package com.qodein.feature.auth

import com.qodein.core.model.Country
import com.qodein.core.model.PhoneNumber

data class AuthViewState(
    val selectedCountry: Country? = null,
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isPhoneNumberValid: Boolean
        get() = selectedCountry?.let {
            PhoneNumber(phoneNumber, it).isValid
        } ?: false

    val fullPhoneNumber: String
        get() = selectedCountry?.let {
            PhoneNumber(phoneNumber, it).fullNumber
        } ?: ""
}
