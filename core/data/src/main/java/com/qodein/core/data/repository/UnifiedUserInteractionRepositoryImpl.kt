package com.qodein.core.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.qodein.core.data.datasource.FirestoreUnifiedUserInteractionDataSource
import com.qodein.core.data.mapper.UserInteractionMapper
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.InteractionError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState
import timber.log.Timber
import java.io.IOException

/**
 * Repository implementation for unified user interactions.
 * Returns Result<D, OperationError> for type-safe error handling.
 */

class UnifiedUserInteractionRepositoryImpl(
    private val dataSource: FirestoreUnifiedUserInteractionDataSource,
    private val mapper: UserInteractionMapper
) : UnifiedUserInteractionRepository {

    /**
     * Maps FirebaseFirestoreException to domain-specific OperationError for voting operations.
     * Uses shared ErrorMapper for logging, then maps to voting-specific errors.
     */
    private fun mapFirestoreErrorForVoting(e: FirebaseFirestoreException): OperationError {
        ErrorMapper.mapFirestoreException(e)
        return when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                InteractionError.VotingFailure.NotAuthorized
            FirebaseFirestoreException.Code.NOT_FOUND ->
                InteractionError.VotingFailure.ContentNotFound
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                SystemError.Offline
            else ->
                SystemError.Unknown
        }
    }

    /**
     * Maps FirebaseFirestoreException to domain-specific OperationError for bookmark operations.
     * Uses shared ErrorMapper for logging, then maps to bookmark-specific errors.
     */
    private fun mapFirestoreErrorForBookmark(e: FirebaseFirestoreException): OperationError {
        ErrorMapper.mapFirestoreException(e)
        return when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                InteractionError.BookmarkFailure.NotAuthorized
            FirebaseFirestoreException.Code.NOT_FOUND ->
                InteractionError.BookmarkFailure.ContentNotFound
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                SystemError.Offline
            else ->
                SystemError.Unknown
        }
    }

    override suspend fun getUserInteraction(
        itemId: String,
        userId: UserId
    ): Result<UserInteraction?, OperationError> =
        try {
            val dto = dataSource.getUserInteraction(itemId, userId.value)
            val interaction = dto?.let { mapper.fromDto(it) }
            Result.Success(interaction)
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to get interaction - unauthorized: item=%s, user=%s", itemId, userId.value)
            Result.Error(InteractionError.VotingFailure.NotAuthorized)
        } catch (e: IOException) {
            Timber.e(e, "Failed to get interaction - network: item=%s, user=%s", itemId, userId.value)
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get interaction - unknown: item=%s, user=%s", itemId, userId.value)
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

    override suspend fun toggleVote(
        itemId: String,
        itemType: ContentType,
        userId: UserId,
        newVoteState: VoteState
    ): Result<UserInteraction, OperationError> =
        try {
            Timber.d("Toggling vote: item=%s, user=%s, targetState=%s", itemId, userId.value, newVoteState)

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

            Timber.i("Vote toggled successfully: item=%s, finalState=%s", itemId, savedInteraction.voteState)
            Result.Success(savedInteraction)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Failed to toggle vote - Firestore error: item=%s, user=%s, code=%s", itemId, userId.value, e.code)
            Result.Error(mapFirestoreErrorForVoting(e))
        } catch (e: IOException) {
            Timber.e(e, "Failed to toggle vote - network: item=%s, user=%s", itemId, userId.value)
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle vote - unknown: item=%s, user=%s", itemId, userId.value)
            Result.Error(InteractionError.VotingFailure.SaveFailed)
        }

    override suspend fun toggleBookmark(
        itemId: String,
        itemType: ContentType,
        userId: UserId
    ): Result<UserInteraction, OperationError> =
        try {
            Timber.d("Toggling bookmark: item=%s, user=%s", itemId, userId.value)

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

            Timber.i("Bookmark toggled successfully: item=%s, bookmarked=%s", itemId, savedInteraction.isBookmarked)
            Result.Success(savedInteraction)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Failed to toggle bookmark - Firestore error: item=%s, user=%s, code=%s", itemId, userId.value, e.code)
            Result.Error(mapFirestoreErrorForBookmark(e))
        } catch (e: IOException) {
            Timber.e(e, "Failed to toggle bookmark - network: item=%s, user=%s", itemId, userId.value)
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle bookmark - unknown: item=%s, user=%s", itemId, userId.value)
            Result.Error(InteractionError.BookmarkFailure.SaveFailed)
        }
}
