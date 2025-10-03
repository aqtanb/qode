package com.qodein.shared.domain

import com.qodein.shared.model.User
import com.qodein.shared.model.UserId

/**
 * Represents the authentication state of the user.
 *
 * Simplified to focus only on essential domain states. Loading and error states
 * are handled in the UI layer where they occur (AuthViewModel, ProfileViewModel, etc.)
 */
sealed interface AuthState {

    /**
     * User is not authenticated
     */
    data object Unauthenticated : AuthState

    /**
     * User is authenticated with their profile data
     */
    data class Authenticated(val user: User) : AuthState
}

/**
 * Returns the UserId if authenticated, null otherwise.
 */
val AuthState.userIdOrNull: UserId?
    get() = (this as? AuthState.Authenticated)?.user?.id
