package com.qodein.feature.promocode.submission

import com.qodein.shared.model.User

/**
 * Clean authentication domain state without UI concerns.
 *
 * Each state stores only essential domain data.
 * UI behavior should be derived in presentation layer.
 */
sealed interface AuthenticationState {
    data object Loading : AuthenticationState
    data object Unauthenticated : AuthenticationState
    data class Authenticated(val user: User) : AuthenticationState
    data class Error(val throwable: Throwable) : AuthenticationState
}
