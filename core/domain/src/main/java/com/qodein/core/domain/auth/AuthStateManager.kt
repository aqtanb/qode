package com.qodein.core.domain.auth

import com.qodein.core.domain.AuthState
import com.qodein.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized auth state management following enterprise patterns.
 *
 * Responsibilities:
 * - Convert repository auth flow to domain AuthState
 * - Handle user model creation from auth data
 * - Provide clean interface for auth state observation
 *
 * Benefits:
 * - Single source of truth for auth state
 * - Testable (no direct Firebase dependency)
 * - Follows dependency inversion principle
 */
@Singleton
class AuthStateManager @Inject constructor(private val authRepository: AuthRepository) {

    /**
     * Observe authentication state changes
     *
     * @return Flow of AuthState with proper domain models
     */
    fun getAuthState(): Flow<AuthState> =
        authRepository.getAuthStateFlow()
            .map { user ->
                when (user) {
                    null -> AuthState.Unauthenticated
                    else -> AuthState.Authenticated(user)
                }
            }

    /**
     * Check if user is currently authenticated
     *
     * @return true if user is signed in, false otherwise
     */
    fun isUserAuthenticated(): Boolean = authRepository.isSignedIn()
}
