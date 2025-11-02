package com.qodein.feature.auth

sealed interface AuthEvent {
    data object SignedIn : AuthEvent
}
