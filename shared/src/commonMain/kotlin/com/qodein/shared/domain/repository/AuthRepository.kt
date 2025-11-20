package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * Returns Result<D, OperationError> for type-safe error handling.
 * Repository implementations handle exception translation to domain errors.
 */
interface AuthRepository {

    /**
     * Sign in with Google authentication.
     */
    suspend fun signInWithGoogle(): Result<User, OperationError>

    /**
     * Sign out the current user.
     */
    suspend fun signOut(): Result<Unit, OperationError>

    /**
     * Observe authentication state changes.
     * Emits User when signed in, null when signed out.
     * This method doesn't return errors - authentication state changes
     * are delivered as Flow emissions.
     */
    suspend fun getAuthStateFlow(): Flow<User?>
}
