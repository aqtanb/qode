package com.qodein.shared.data.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.common.flatMap
import com.qodein.shared.data.datasource.SupabaseUserDataSource
import com.qodein.shared.data.mapper.toDomain
import com.qodein.shared.data.mapper.toInsertDto
import com.qodein.shared.data.util.PageCursor
import com.qodein.shared.data.util.remoteCall
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.BlocksSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.User
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepositoryImpl(private val dataSource: SupabaseUserDataSource) : UserRepository {
    override suspend fun getUserById(userId: String): Result<User, OperationError> =
        remoteCall("Get user") { dataSource.getUserById(userId) }.flatMap { dto ->
            dto?.let { Result.Success(it.toDomain()) } ?: Result.Error(UserError.ProfileFailure.NotFound)
        }

    override suspend fun createUser(user: User): Result<Unit, OperationError> =
        remoteCall("Create user") { dataSource.createUser(user.toInsertDto()) }

    override suspend fun updateUserConsent(
        userId: String,
        legalPoliciesAcceptedAt: Long
    ): Result<Unit, OperationError> = remoteCall("Update consent") { dataSource.updateUserConsent(userId, legalPoliciesAcceptedAt) }

    /** Stats change from triggers, so collectors see the latest row each time they start. */
    override fun observeUser(userId: String): Flow<Result<User, OperationError>> = flow { emit(getUserById(userId)) }

    override suspend fun blockUser(
        currentUserId: String,
        blockedUserId: String
    ): Result<Unit, OperationError> = remoteCall("Block user") { dataSource.blockUser(currentUserId, blockedUserId) }

    override suspend fun unblockUser(
        currentUserId: String,
        blockedUserId: String
    ): Result<Unit, OperationError> = remoteCall("Unblock user") { dataSource.unblockUser(currentUserId, blockedUserId) }

    override suspend fun getBlockedUserIds(currentUserId: UserId): Set<UserId> =
        when (val result = remoteCall("Get blocked IDs") { dataSource.getBlockedUserIds(currentUserId.value) }) {
            is Result.Success -> result.data.map { UserId(it) }.toSet()
            is Result.Error -> emptySet()
        }

    override suspend fun getBlockedUsers(
        currentUserId: String,
        cursor: Any?,
        limit: Int
    ): Result<PaginatedResult<User, BlocksSortBy>, OperationError> =
        remoteCall("Get blocked users") {
            val pageCursor = (cursor as? PaginationCursor<*>)?.value as? PageCursor
            val page = dataSource.getBlockedUsers(currentUserId, limit, pageCursor)
            val users = page.items.mapNotNull { block -> dataSource.getUserById(block.blockedUserId)?.toDomain() }
            PaginatedResult(users, page.nextCursor?.let { PaginationCursor(it, BlocksSortBy.NEWEST) })
        }

    override suspend fun deleteUserAccount(userId: String): Result<Unit, OperationError> =
        try {
            dataSource.deleteUserAccount()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: RestException) {
            Result.Error(if (e.statusCode == 401) UserError.DeletionFailure.NotAuthenticated else UserError.DeletionFailure.DeleteFailed)
        } catch (e: Exception) {
            Result.Error(SystemError.Offline)
        }
}

class UnifiedUserInteractionRepositoryImpl(private val dataSource: SupabaseUserDataSource) : UnifiedUserInteractionRepository {
    override suspend fun getUserInteraction(
        documentId: String,
        userId: UserId
    ): Result<UserInteraction?, OperationError> =
        remoteCall("Get interaction") {
            val itemType = documentId.substringBefore(ID_SEPARATOR)
            val itemId = documentId.substringAfter(ID_SEPARATOR)
            dataSource.getUserInteraction(itemType, itemId, userId.value)?.toDomain()
        }

    override suspend fun toggleVote(interaction: UserInteraction): Result<UserInteraction, OperationError> =
        remoteCall("Save interaction") {
            val itemType = interaction.itemType.name
            if (interaction.voteState == VoteState.NONE && !interaction.isBookmarked) {
                dataSource.deleteUserInteraction(itemType, interaction.itemId, interaction.userId.value)
            } else {
                dataSource.saveUserInteraction(itemType, interaction.itemId, interaction.voteState.name, interaction.isBookmarked)
            }
            interaction
        }

    private companion object {
        const val ID_SEPARATOR = "_"
    }
}
