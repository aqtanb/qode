package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.User

/**
 * Repository interface for user operations.
 * Handles user statistics updates for gamification and leaderboards.
 */
interface UserRepository {

    /**
     * Get user by ID with current stats from Firestore.
     */
    suspend fun getUserById(userId: String): Result<User, OperationError>

    /**
     * Create user document in Firestore if it doesn't exist.
     * If user already exists, this is a no-op.
     */
    suspend fun createUser(user: User): Result<Unit, OperationError>

    /**
     * Increment the user's submitted promocodes count by 1.
     */
    suspend fun incrementPromocodeCount(userId: String): Result<Unit, OperationError>

    /**
     * Increment the user's submitted posts count by 1.
     */
    suspend fun incrementPostCount(userId: String): Result<Unit, OperationError>
}
