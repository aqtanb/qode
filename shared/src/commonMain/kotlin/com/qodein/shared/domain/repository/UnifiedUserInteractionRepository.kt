package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for unified user interactions (votes + bookmarks).
 * Returns Result<D, OperationError> for type-safe error handling.
 * Repository implementations handle exception translation to domain errors.
 */
interface UnifiedUserInteractionRepository {

    // Single interaction operations

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
     * Observe user interaction for real-time updates.
     */
    fun observeUserInteraction(
        itemId: String,
        userId: UserId
    ): Flow<Result<UserInteraction?, OperationError>>

    // Batch operations

    /**
     * Get all bookmarked content for a user.
     */
    suspend fun getUserBookmarks(userId: UserId): Result<List<UserInteraction>, OperationError>

    /**
     * Get all user interactions for specific user.
     */
    suspend fun getAllUserInteractions(userId: UserId): Result<List<UserInteraction>, OperationError>

    /**
     * Get user interactions for multiple content items.
     */
    suspend fun getUserInteractionsForItems(
        itemIds: List<String>,
        userId: UserId
    ): Result<Map<String, UserInteraction>, OperationError>

    // Content-centric operations

    /**
     * Get all interactions for specific content.
     */
    suspend fun getInteractionsForContent(itemId: String): Result<List<UserInteraction>, OperationError>

    // Convenience methods

    /**
     * Toggle user's vote on content.
     */
    suspend fun toggleVote(
        itemId: String,
        itemType: ContentType,
        userId: UserId,
        newVoteState: com.qodein.shared.model.VoteState
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
