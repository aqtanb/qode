package com.qodein.feature.auth

sealed interface AuthEvent {
    data object SignedIn : AuthEvent
    data object ShowTermsOfService : AuthEvent
    data object ShowPrivacyPolicy : AuthEvent
}
