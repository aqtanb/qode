package com.qodein.shared.domain.auth

import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
 *
 * Note: DI configuration is handled in platform-specific modules
 */
class AuthStateManager(private val authRepository: AuthRepository) {
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
}
