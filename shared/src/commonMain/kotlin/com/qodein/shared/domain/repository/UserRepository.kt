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

    /**
     * Get user by ID with current stats from Firestore.
     */
    fun getUserById(userId: String): Flow<Result<User, OperationError>>

    /**
     * Create user document in Firestore if it doesn't exist.
     * If user already exists, this is a no-op.
     */
    fun createUser(user: User): Flow<Result<Unit, OperationError>>

    /**
     * Increment the user's submitted promocodes count by 1.
     */
    fun incrementPromocodeCount(userId: String): Flow<Result<Unit, OperationError>>

    /**
     * Increment the user's submitted posts count by 1.
     */
    fun incrementPostCount(userId: String): Flow<Result<Unit, OperationError>>
}
