package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.GoogleAuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /**
     * Sign in with Google authentication.
     * Returns auth result with user info - use case handles user creation/fetching.
     */
    suspend fun signInWithGoogle(idToken: String): Result<GoogleAuthResult, OperationError>

    /**
     * Sign out the current user. Local operation, cannot fail.
     */
    fun signOut()

    /**
     * Observe authentication state changes.
     * Emits User when signed in, null when signed out.
     */
    fun getAuthStateFlow(): Flow<GoogleAuthResult?>
}
