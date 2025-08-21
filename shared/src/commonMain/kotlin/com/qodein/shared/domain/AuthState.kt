package com.qodein.shared.domain

import com.qodein.shared.model.User

/**
 * Represents the authentication state of the user.
 *
 * Simplified to focus only on essential states - auth errors are handled
 * in the UI layer where they occur (AuthViewModel, ProfileViewModel, etc.)
 */
sealed interface AuthState {

    /**
     * Authentication state is being determined
     */
    data object Loading : AuthState

    /**
     * User is not authenticated
     */
    data object Unauthenticated : AuthState

    /**
     * User is authenticated with their profile data
     */
    data class Authenticated(val user: User) : AuthState
}
