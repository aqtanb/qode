package com.qodein.shared.domain.repository

import com.qodein.shared.model.ContentType
import com.qodein.shared.model.InteractionStats
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for unified user interactions (votes + bookmarks).
 * Replaces separate VoteRepository and UserBookmarkRepository for cost efficiency.
 *
 * All methods may throw standard exceptions which will be handled by use cases
 * using the .asResult() extension.
 *
 * Follows NIA pattern - uses standard exceptions, no custom exception hierarchies.
 */
interface UnifiedUserInteractionRepository {

    // ================================================================================================
    // SINGLE INTERACTION OPERATIONS
    // ================================================================================================

    /**
     * Get user interaction for specific content and user.
     * Returns null if no interaction exists.
     *
     * @param itemId The content ID (PromoCodeId.value, PostId.value, etc.)
     * @param userId The user ID
     * @return UserInteraction or null if not found
     * @throws IOException if network/database error occurs
     */
    suspend fun getUserInteraction(
        itemId: String,
        userId: UserId
    ): UserInteraction?

    /**
     * Create or update user interaction.
     * Uses upsert semantics - creates if doesn't exist, updates if exists.
     *
     * @param interaction The interaction to save
     * @return The saved interaction
     * @throws IOException if network/database error occurs
     * @throws SecurityException if user lacks permission
     */
    suspend fun upsertUserInteraction(interaction: UserInteraction): UserInteraction

    /**
     * Delete user interaction.
     * No-op if interaction doesn't exist.
     *
     * @param itemId The content ID
     * @param userId The user ID
     * @throws IOException if network/database error occurs
     * @throws SecurityException if user lacks permission
     */
    suspend fun deleteUserInteraction(
        itemId: String,
        userId: UserId
    )

    /**
     * Observe user interaction for real-time updates.
     * Emits null if no interaction exists.
     *
     * @param itemId The content ID
     * @param userId The user ID
     * @return Flow of UserInteraction or null
     */
    fun observeUserInteraction(
        itemId: String,
        userId: UserId
    ): Flow<UserInteraction?>

    // ================================================================================================
    // BATCH OPERATIONS
    // ================================================================================================

    /**
     * Get all bookmarked content for a user.
     * Returns interactions where isBookmarked = true, ordered by most recently updated.
     *
     * @param userId The user ID
     * @return List of UserInteraction for bookmarked content
     * @throws IOException if network/database error occurs
     */
    suspend fun getUserBookmarks(userId: UserId): List<UserInteraction>

    /**
     * Get all user interactions for specific user.
     * Useful for user profile, analytics, etc.
     *
     * @param userId The user ID
     * @return List of all UserInteraction for the user
     * @throws IOException if network/database error occurs
     */
    suspend fun getAllUserInteractions(userId: UserId): List<UserInteraction>

    /**
     * Get user interactions for multiple content items.
     * Optimized for feed screens where we need user state for multiple items.
     *
     * @param itemIds List of content IDs
     * @param userId The user ID
     * @return Map of itemId to UserInteraction (only includes items with interactions)
     * @throws IOException if network/database error occurs
     */
    suspend fun getUserInteractionsForItems(
        itemIds: List<String>,
        userId: UserId
    ): Map<String, UserInteraction>

    // ================================================================================================
    // CONTENT-CENTRIC OPERATIONS (FOR ANALYTICS)
    // ================================================================================================

    /**
     * Get all interactions for specific content.
     * Useful for analytics, moderation, etc.
     *
     * @param itemId The content ID
     * @return List of UserInteraction for the content
     * @throws IOException if network/database error occurs
     */
    suspend fun getInteractionsForContent(itemId: String): List<UserInteraction>

    /**
     * Get interaction counts for content.
     * Returns aggregated stats without user details.
     *
     * @param itemId The content ID
     * @return InteractionStats with vote/bookmark counts
     * @throws IOException if network/database error occurs
     */
    suspend fun getInteractionStats(itemId: String): InteractionStats

    // ================================================================================================
    // CONVENIENCE METHODS
    // ================================================================================================

    /**
     * Toggle user's vote on content.
     * If user has same vote, removes it. If different vote or no vote, sets new vote.
     *
     * @param itemId The content ID
     * @param itemType The content type
     * @param userId The user ID
     * @param newVoteState The vote state to toggle to
     * @return Updated UserInteraction
     */
    suspend fun toggleVote(
        itemId: String,
        itemType: ContentType,
        userId: UserId,
        newVoteState: com.qodein.shared.model.VoteState
    ): UserInteraction

    /**
     * Toggle user's bookmark on content.
     *
     * @param itemId The content ID
     * @param itemType The content type
     * @param userId The user ID
     * @return Updated UserInteraction
     */
    suspend fun toggleBookmark(
        itemId: String,
        itemType: ContentType,
        userId: UserId
    ): UserInteraction
}
