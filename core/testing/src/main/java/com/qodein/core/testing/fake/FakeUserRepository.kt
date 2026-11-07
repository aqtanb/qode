package com.qodein.core.testing.fake

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.BlocksSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.User
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {
    val users = mutableMapOf<String, User>()

    /** When set, getUserById fails with this error. */
    var getUserError: OperationError? = null

    override suspend fun getUserById(userId: String): Result<User, OperationError> {
        getUserError?.let { return Result.Error(it) }
        return users[userId]?.let { Result.Success(it) } ?: Result.Error(UserError.ProfileFailure.NotFound)
    }

    override suspend fun createUser(user: User): Result<Unit, OperationError> {
        users[user.id.value] = user
        return Result.Success(Unit)
    }

    override suspend fun updateUserConsent(
        userId: String,
        legalPoliciesAcceptedAt: Long
    ): Result<Unit, OperationError> = Result.Success(Unit)

    override fun observeUser(userId: String): Flow<Result<User, OperationError>> =
        flowOf(users[userId]?.let { Result.Success(it) } ?: Result.Error(UserError.ProfileFailure.NotFound))

    override suspend fun blockUser(
        currentUserId: String,
        blockedUserId: String
    ): Result<Unit, OperationError> = Result.Success(Unit)

    override suspend fun unblockUser(
        currentUserId: String,
        blockedUserId: String
    ): Result<Unit, OperationError> = Result.Success(Unit)

    override suspend fun getBlockedUserIds(currentUserId: UserId): Set<UserId> = emptySet()

    override suspend fun getBlockedUsers(
        currentUserId: String,
        cursor: Any?,
        limit: Int
    ): Result<PaginatedResult<User, BlocksSortBy>, OperationError> = error("Not used in tests")

    override suspend fun deleteUserAccount(userId: String): Result<Unit, OperationError> {
        users.remove(userId)
        return Result.Success(Unit)
    }
}
