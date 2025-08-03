package com.qodein.feature.auth

sealed interface AuthAction {
    data object SignInWithGoogleClicked : AuthAction
}
