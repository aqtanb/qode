package com.qodein.core.model

data class PhoneNumber(val number: String, val country: Country) {
    val sanitizedNumber: String
        get() = number.filter { it.isDigit() }

    val isValid: Boolean
        get() = sanitizedNumber.length in 6..15

    val fullNumber: String
        get() = "${country.phoneCode}$sanitizedNumber"

    companion object {
        fun create(
            rawNumber: String,
            country: Country
        ): PhoneNumber {
            val cleanNumber = rawNumber
                .removePrefix(country.phoneCode)
                .filter { it.isDigit() }

            return PhoneNumber(
                number = cleanNumber,
                country = country,
            )
        }
    }
}
