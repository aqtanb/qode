package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.GoogleAuthResult
import com.qodein.shared.model.GoogleIdToken
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /**
     * Sign in with Google authentication.
     * Returns auth result with user info - use case handles user creation/fetching.
     */
    suspend fun signInWithGoogle(token: GoogleIdToken): Result<GoogleAuthResult, OperationError>

    /**
     * Send a one-time sign-in code to [email]. Creates the auth user if it doesn't exist.
     */
    suspend fun sendEmailCode(email: String): Result<Unit, OperationError>

    /**
     * Sign in with the code sent by [sendEmailCode].
     */
    suspend fun signInWithEmailCode(
        email: String,
        code: String
    ): Result<GoogleAuthResult, OperationError>

    /**
     * Sign out the current user.
     */
    suspend fun signOut()

    /**
     * Observe authentication state changes.
     * Emits User when signed in, null when signed out.
     */
    fun observeAuthState(): Flow<GoogleAuthResult?>
}
