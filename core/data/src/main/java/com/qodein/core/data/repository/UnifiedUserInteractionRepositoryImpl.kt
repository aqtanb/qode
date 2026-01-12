package com.qodein.core.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.qodein.core.data.datasource.FirestoreUserDataSource
import com.qodein.core.data.mapper.UserInteractionMapper
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.InteractionError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState
import timber.log.Timber
import java.io.IOException

/**
 * Repository implementation for unified user interactions.
 * Returns Result<D, OperationError> for type-safe error handling.
 */

class UnifiedUserInteractionRepositoryImpl(private val userDataSource: FirestoreUserDataSource) : UnifiedUserInteractionRepository {

    override suspend fun getUserInteraction(
        documentId: String,
        userId: UserId
    ): Result<UserInteraction?, OperationError> =
        try {
            val dto = userDataSource.getUserInteraction(documentId, userId.value)
            val interaction = dto?.let { UserInteractionMapper.fromDto(it) }
            Result.Success(interaction)
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to get interaction - unauthorized: doc=%s, user=%s", documentId, userId.value)
            Result.Error(InteractionError.VotingFailure.NotAuthorized)
        } catch (e: IOException) {
            Timber.e(e, "Failed to get interaction - network: doc=%s, user=%s", documentId, userId.value)
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get interaction - unknown: doc=%s, user=%s", documentId, userId.value)
            Result.Error(SystemError.Unknown)
        }

    override suspend fun toggleVote(interaction: UserInteraction): Result<UserInteraction, OperationError> =
        try {
            Timber.d("Saving interaction: doc=%s, user=%s, voteState=%s", interaction.id, interaction.userId.value, interaction.voteState)

            // Delete document if interaction is empty (no vote and no bookmark)
            if (interaction.voteState == VoteState.NONE && !interaction.isBookmarked) {
                userDataSource.deleteUserInteraction(interaction.id, interaction.userId.value)
                Timber.i("Interaction deleted (empty): doc=%s", interaction.id)
            } else {
                // Save interaction
                val dto = UserInteractionMapper.toDto(interaction)
                userDataSource.setUserInteraction(dto)
                Timber.i("Interaction saved: doc=%s, voteState=%s", interaction.id, interaction.voteState)
            }

            Result.Success(interaction)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(
                e,
                "Failed to toggle vote - Firestore error: doc=%s, user=%s, code=%s",
                interaction.id,
                interaction.userId.value,
                e.code,
            )
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IOException) {
            Timber.e(e, "Failed to toggle vote - network: doc=%s, user=%s", interaction.id, interaction.userId.value)
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle vote - unknown: doc=%s, user=%s", interaction.id, interaction.userId.value)
            Result.Error(InteractionError.VotingFailure.SaveFailed)
        }
}
