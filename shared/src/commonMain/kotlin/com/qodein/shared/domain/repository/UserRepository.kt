package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user operations.
 * Handles user statistics updates for gamification and leaderboards.
 */
interface UserRepository {

    suspend fun getUserById(userId: String): Result<User, OperationError>

    suspend fun createUser(user: User): Result<Unit, OperationError>

    suspend fun updateUserConsent(
        userId: String,
        legalPoliciesAcceptedAt: Long
    ): Result<Unit, OperationError>

    fun observeUser(userId: String): Flow<Result<User, OperationError>>

    suspend fun blockUser(
        currentUserId: String,
        blockedUserId: String
    ): Result<Unit, OperationError>

    fun getBlockedUserIds(currentUserId: String): Flow<Set<String>>

    /**
     * Deletes the user account and all associated data.
     * This operation is irreversible.
     *
     * @param userId The ID of the user to delete
     * @return Result indicating success or specific deletion failure
     */
    suspend fun deleteUserAccount(userId: String): Result<Unit, OperationError>
}
