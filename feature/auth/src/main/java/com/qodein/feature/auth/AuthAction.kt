package com.qodein.feature.auth

sealed interface AuthAction {
    data object SignInWithGoogleClicked : AuthAction
    data object RetryClicked : AuthAction
    data object TermsOfServiceClicked : AuthAction
    data object PrivacyPolicyClicked : AuthAction
}
