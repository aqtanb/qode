package com.qodein.feature.auth

sealed interface AuthEvent {
    data object SignedIn : AuthEvent
    data object TermsOfServiceRequested : AuthEvent
    data object PrivacyPolicyRequested : AuthEvent
}
