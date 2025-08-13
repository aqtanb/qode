package com.qodein.core.domain.auth

import com.qodein.core.domain.AuthState
import com.qodein.core.domain.repository.AuthRepository
import com.qodein.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise-level centralized auth state management.
 *
 * This is the SINGLE SOURCE OF TRUTH for all authentication state in the app.
 * All other components should use this class instead of accessing repositories directly.
 *
 * Responsibilities:
 * - Convert repository auth flow to domain AuthState
 * - Provide reactive auth state observation
 * - Provide synchronous auth state checks when needed
 * - Handle user model access
 *
 * Benefits:
 * - Single source of truth for auth state
 * - Consistent interface across the app
 * - Testable (no direct Firebase dependency)
 * - Follows dependency inversion principle
 */
@Singleton
class AuthStateManager @Inject constructor(private val authRepository: AuthRepository) {

    // Cache current state for sync access
    private var currentState: AuthState = AuthState.Loading

    /**
     * Observe authentication state changes
     *
     * @return Flow of AuthState with proper domain models
     */
    fun getAuthState(): Flow<AuthState> =
        authRepository.getAuthStateFlow()
            .map { user ->
                val newState = when (user) {
                    null -> AuthState.Unauthenticated
                    else -> AuthState.Authenticated(user)
                }
                currentState = newState // Cache for sync access
                newState
            }

    /**
     * Check if user is currently authenticated (synchronous)
     *
     * @return true if user is signed in, false otherwise
     */
    fun isUserAuthenticated(): Boolean = authRepository.isSignedIn()

    /**
     * Get current authenticated user (synchronous)
     *
     * @return current User if authenticated, null otherwise
     */
    fun getCurrentUser(): User? =
        when (val state = currentState) {
            is AuthState.Authenticated -> state.user
            else -> null
        }

    /**
     * Get current auth state (synchronous)
     *
     * @return current AuthState
     */
    fun getCurrentAuthState(): AuthState = currentState
}
