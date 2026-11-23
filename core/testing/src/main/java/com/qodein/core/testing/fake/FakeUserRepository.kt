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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class FakeUserRepository : UserRepository {
    val users = mutableMapOf<String, User>()

    /** Like the real repository, observeUser re-emits after createUser for that user. */
    private val changedUserIds = MutableSharedFlow<String>(extraBufferCapacity = 8)

    /** When set, getUserById fails with this error. */
    var getUserError: OperationError? = null

    override suspend fun getUserById(userId: String): Result<User, OperationError> {
        getUserError?.let { return Result.Error(it) }
        return users[userId]?.let { Result.Success(it) } ?: Result.Error(UserError.ProfileFailure.NotFound)
    }

    override suspend fun createUser(user: User): Result<Unit, OperationError> {
        users[user.id.value] = user
        changedUserIds.tryEmit(user.id.value)
        return Result.Success(Unit)
    }

    override suspend fun updateUserConsent(
        userId: String,
        legalPoliciesAcceptedAt: Long
    ): Result<Unit, OperationError> = Result.Success(Unit)

    /** The last name passed to updateDisplayName, per user ID. */
    val displayNameUpdates = mutableMapOf<String, String?>()

    override suspend fun updateDisplayName(
        userId: String,
        displayName: String?
    ): Result<Unit, OperationError> {
        displayNameUpdates[userId] = displayName
        return Result.Success(Unit)
    }

    override fun observeUser(userId: String): Flow<Result<User, OperationError>> =
        changedUserIds.filter { it == userId }.onStart { emit(userId) }.map { getUserById(userId) }

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
