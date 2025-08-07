package com.qodein.core.domain.usecase.auth

import com.qodein.core.domain.AuthState
import com.qodein.core.domain.auth.AuthStateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for observing authentication state following NIA patterns.
 *
 * Wraps AuthStateManager with Result pattern for proper error handling.
 * This allows the UI layer to handle auth state errors gracefully.
 */
@Singleton
class GetAuthStateUseCase @Inject constructor(private val authStateManager: AuthStateManager) {

    /**
     * Get authentication state as Flow<Result<AuthState>>
     *
     * @return Flow that emits Result.success(AuthState) or Result.failure(Throwable)
     */
    operator fun invoke(): Flow<Result<AuthState>> =
        authStateManager.getAuthState()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }

    /**
     * Check if user is currently authenticated synchronously
     *
     * @return true if user is signed in, false otherwise
     */
    fun isAuthenticated(): Boolean = authStateManager.isUserAuthenticated()
}
