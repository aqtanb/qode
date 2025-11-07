package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState

/**
 * Repository interface for unified user interactions (votes + bookmarks).
 * Returns Result<D, OperationError> for type-safe error handling.
 * Repository implementations handle exception translation to domain errors.
 */
interface UnifiedUserInteractionRepository {

    /**
     * Get user interaction for specific content and user.
     */
    suspend fun getUserInteraction(
        itemId: String,
        userId: UserId
    ): Result<UserInteraction?, OperationError>

    /**
     * Create or update user interaction.
     */
    suspend fun upsertUserInteraction(interaction: UserInteraction): Result<UserInteraction, OperationError>

    /**
     * Delete user interaction.
     */
    suspend fun deleteUserInteraction(
        itemId: String,
        userId: UserId
    ): Result<Unit, OperationError>

    /**
     * Get all bookmarked content for a user.
     */
    suspend fun getUserBookmarks(userId: UserId): Result<List<UserInteraction>, OperationError>

    /**
     * Get all user interactions for specific user.
     */
    suspend fun getAllUserInteractions(userId: UserId): Result<List<UserInteraction>, OperationError>

    /**
     * Toggle user's vote on content.
     */
    suspend fun toggleVote(
        itemId: String,
        itemType: ContentType,
        userId: UserId,
        newVoteState: VoteState
    ): Result<UserInteraction, OperationError>

    /**
     * Toggle user's bookmark on content.
     */
    suspend fun toggleBookmark(
        itemId: String,
        itemType: ContentType,
        userId: UserId
    ): Result<UserInteraction, OperationError>
}
