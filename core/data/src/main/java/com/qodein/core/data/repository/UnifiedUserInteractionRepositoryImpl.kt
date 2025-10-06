package com.qodein.core.data.repository

import co.touchlab.kermit.Logger
import com.qodein.core.data.datasource.FirestoreUnifiedUserInteractionDataSource
import com.qodein.core.data.mapper.UserInteractionMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.InteractionError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for unified user interactions.
 * Returns Result<D, OperationError> for type-safe error handling.
 */
@Singleton
class UnifiedUserInteractionRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreUnifiedUserInteractionDataSource,
    private val mapper: UserInteractionMapper
) : UnifiedUserInteractionRepository {

    companion object {
        private const val TAG = "UserInteractionRepo"
    }

    // Single interaction operations

    override suspend fun getUserInteraction(
        itemId: String,
        userId: UserId
    ): Result<UserInteraction?, OperationError> =
        try {
            val dto = dataSource.getUserInteraction(itemId, userId.value)
            val interaction = dto?.let { mapper.fromDto(it) }
            Result.Success(interaction)
        } catch (e: SecurityException) {
            Logger.e(TAG, e) { "Failed to get interaction - unauthorized: item=$itemId, user=${userId.value}" }
            Result.Error(InteractionError.VotingFailure.NotAuthorized)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Failed to get interaction - network: item=$itemId, user=${userId.value}" }
            Result.Error(SystemError.Offline)
        } catch (e: IllegalStateException) {
            Logger.e(TAG, e) { "Failed to get interaction - service down: item=$itemId, user=${userId.value}" }
            Result.Error(SystemError.ServiceDown)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Failed to get interaction - unknown: item=$itemId, user=${userId.value}" }
            Result.Error(SystemError.Unknown)
        }

    override suspend fun upsertUserInteraction(interaction: UserInteraction): Result<UserInteraction, OperationError> =
        try {
            val dto = mapper.toDto(interaction)
            val savedDto = dataSource.upsertUserInteraction(dto)
            val savedInteraction = mapper.fromDto(savedDto) ?: interaction
            Result.Success(savedInteraction)
        } catch (e: SecurityException) {
            Result.Error(InteractionError.VotingFailure.NotAuthorized)
        } catch (e: IOException) {
            Result.Error(SystemError.Offline)
        } catch (e: IllegalStateException) {
            Result.Error(SystemError.ServiceDown)
        } catch (e: Exception) {
            Result.Error(InteractionError.VotingFailure.SaveFailed)
        }

    override suspend fun deleteUserInteraction(
        itemId: String,
        userId: UserId
    ): Result<Unit, OperationError> =
        try {
            dataSource.deleteUserInteraction(itemId, userId.value)
            Result.Success(Unit)
        } catch (e: SecurityException) {
            Result.Error(InteractionError.BookmarkFailure.NotAuthorized)
        } catch (e: IOException) {
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Result.Error(InteractionError.BookmarkFailure.RemoveFailed)
        }

    override fun observeUserInteraction(
        itemId: String,
        userId: UserId
    ): Flow<Result<UserInteraction?, OperationError>> =
        flow {
            try {
                dataSource.observeUserInteraction(itemId, userId.value).collect { dto ->
                    val interaction = dto?.let { mapper.fromDto(it) }
                    emit(Result.Success(interaction))
                }
            } catch (e: IOException) {
                emit(Result.Error(SystemError.Offline))
            } catch (e: Exception) {
                emit(Result.Error(SystemError.Unknown))
            }
        }

    // Batch operations

    override suspend fun getUserBookmarks(userId: UserId): Result<List<UserInteraction>, OperationError> =
        try {
            val dtos = dataSource.getUserBookmarks(userId.value)
            val interactions = dtos.mapNotNull { mapper.fromDto(it) }
            Result.Success(interactions)
        } catch (e: IOException) {
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Result.Error(InteractionError.BookmarkFailure.ContentNotFound)
        }

    override suspend fun getAllUserInteractions(userId: UserId): Result<List<UserInteraction>, OperationError> =
        try {
            val dtos = dataSource.getAllUserInteractions(userId.value)
            val interactions = dtos.mapNotNull { mapper.fromDto(it) }
            Result.Success(interactions)
        } catch (e: IOException) {
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }

    override suspend fun getUserInteractionsForItems(
        itemIds: List<String>,
        userId: UserId
    ): Result<Map<String, UserInteraction>, OperationError> =
        try {
            val dtosMap = dataSource.getUserInteractionsForItems(itemIds, userId.value)
            val interactionsMap = dtosMap.mapNotNull { (itemId, dto) ->
                mapper.fromDto(dto)?.let { itemId to it }
            }.toMap()
            Result.Success(interactionsMap)
        } catch (e: IOException) {
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }

    // Content-centric operations

    override suspend fun getInteractionsForContent(itemId: String): Result<List<UserInteraction>, OperationError> =
        try {
            val dtos = dataSource.getInteractionsForContent(itemId)
            val interactions = dtos.mapNotNull { mapper.fromDto(it) }
            Result.Success(interactions)
        } catch (e: IOException) {
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Result.Error(InteractionError.VotingFailure.ContentNotFound)
        }

    // Convenience methods

    override suspend fun toggleVote(
        itemId: String,
        itemType: ContentType,
        userId: UserId,
        newVoteState: VoteState
    ): Result<UserInteraction, OperationError> =
        try {
            Logger.d(TAG) { "Toggling vote: item=$itemId, user=${userId.value}, targetState=$newVoteState" }

            // Get existing interaction or create new one
            val existingDto = dataSource.getUserInteraction(itemId, userId.value)
            val existingInteraction = existingDto?.let { mapper.fromDto(it) }

            // Toggle vote state
            val updatedInteraction = if (existingInteraction != null) {
                // If same vote, remove it; if different vote, update it
                val newVote = if (existingInteraction.voteState == newVoteState) VoteState.NONE else newVoteState
                existingInteraction.copy(voteState = newVote)
            } else {
                UserInteraction.create(itemId, itemType, userId, newVoteState, false)
            }

            // Save updated interaction
            val updatedDto = mapper.toDto(updatedInteraction)
            val savedDto = dataSource.upsertUserInteraction(updatedDto)
            val savedInteraction = mapper.fromDto(savedDto) ?: updatedInteraction

            Logger.i(TAG) { "Vote toggled successfully: item=$itemId, finalState=${savedInteraction.voteState}" }
            Result.Success(savedInteraction)
        } catch (e: SecurityException) {
            Logger.e(TAG, e) { "Failed to toggle vote - unauthorized: item=$itemId, user=${userId.value}" }
            Result.Error(InteractionError.VotingFailure.NotAuthorized)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Failed to toggle vote - network: item=$itemId, user=${userId.value}" }
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Failed to toggle vote - unknown: item=$itemId, user=${userId.value}" }
            Result.Error(InteractionError.VotingFailure.SaveFailed)
        }

    override suspend fun toggleBookmark(
        itemId: String,
        itemType: ContentType,
        userId: UserId
    ): Result<UserInteraction, OperationError> =
        try {
            Logger.d(TAG) { "Toggling bookmark: item=$itemId, user=${userId.value}" }

            // Get existing interaction or create new one
            val existingDto = dataSource.getUserInteraction(itemId, userId.value)
            val existingInteraction = existingDto?.let { mapper.fromDto(it) }

            // Toggle bookmark state
            val updatedInteraction = if (existingInteraction != null) {
                existingInteraction.copy(isBookmarked = !existingInteraction.isBookmarked)
            } else {
                // Create new interaction with bookmark
                UserInteraction.create(itemId, itemType, userId, VoteState.NONE, true)
            }

            // Save updated interaction
            val updatedDto = mapper.toDto(updatedInteraction)
            val savedDto = dataSource.upsertUserInteraction(updatedDto)
            val savedInteraction = mapper.fromDto(savedDto) ?: updatedInteraction

            Logger.i(TAG) { "Bookmark toggled successfully: item=$itemId, bookmarked=${savedInteraction.isBookmarked}" }
            Result.Success(savedInteraction)
        } catch (e: SecurityException) {
            Logger.e(TAG, e) { "Failed to toggle bookmark - unauthorized: item=$itemId, user=${userId.value}" }
            Result.Error(InteractionError.BookmarkFailure.NotAuthorized)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Failed to toggle bookmark - network: item=$itemId, user=${userId.value}" }
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Failed to toggle bookmark - unknown: item=$itemId, user=${userId.value}" }
            Result.Error(InteractionError.BookmarkFailure.SaveFailed)
        }
}
