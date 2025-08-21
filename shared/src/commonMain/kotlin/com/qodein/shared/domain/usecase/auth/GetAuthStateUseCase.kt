package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.auth.AuthStateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Use case for observing authentication state following enterprise patterns.
 *
 * Wraps AuthStateManager with Result pattern for proper error handling.
 * This allows the UI layer to handle auth state errors gracefully.
 *
 * Note: For sync auth checks, use AuthStateManager.isUserAuthenticated() directly.
 */

class GetAuthStateUseCase constructor(private val authStateManager: AuthStateManager) {

    /**
     * Get authentication state as Flow<Result<AuthState>>
     *
     * @return Flow that emits Result.success(AuthState) or Result.failure(Throwable)
     */
    operator fun invoke(): Flow<Result<AuthState>> =
        authStateManager.getAuthState()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
