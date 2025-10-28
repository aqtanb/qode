
@file:UseContextualSerialization(Instant::class) // This line is conceptual, you typically add @Contextual per property

package com.qodein.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
enum class VoteState {
    UPVOTE,
    DOWNVOTE,
    NONE
}

@Serializable
enum class ContentType {
    PROMO_CODE,
    POST,
    COMMENT,
    PROMO
}

/**
 * Unified user interaction model combining votes and bookmarks.
 * Stored in single Firestore collection: /user_interactions/{itemId}_{userId}
 *
 * This replaces separate Vote and UserBookmark collections for cost efficiency:
 * - Detail screens: 2 reads instead of 3 (33% cost reduction)
 * - Atomic updates: Vote + bookmark changes in single transaction
 * - Clean separation: Content models have no user-specific fields
 */
@Serializable
data class UserInteraction(
    val id: String, // Generated: sanitized_itemId_sanitized_userId
    val itemId: String, // ID of the content (promo code, post, comment)
    val itemType: ContentType, // Type of content being interacted with (reusing existing ContentType)
    val userId: UserId, // User performing the interaction
    val voteState: VoteState, // Vote state (UPVOTE, DOWNVOTE, or NONE for no vote)
    val isBookmarked: Boolean, // Whether user has bookmarked this content
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
) {
    init {
        require(itemId.isNotBlank()) { "Item ID cannot be blank" }
        require(id.isNotBlank()) { "Interaction ID cannot be blank" }
        require(updatedAt >= createdAt) { "Updated time cannot be before created time" }
        require(id == generateId(itemId, userId.value)) {
            "ID must match format: sanitized_itemId_sanitized_userId"
        }
    }

    /**
     * Toggle upvote state: NONE/DOWNVOTE -> UPVOTE, UPVOTE -> NONE
     */
    fun toggleUpvote(): UserInteraction =
        copy(
            voteState = when (voteState) {
                VoteState.UPVOTE -> VoteState.NONE
                VoteState.DOWNVOTE, VoteState.NONE -> VoteState.UPVOTE
            },
            updatedAt = Clock.System.now(),
        )

    /**
     * Toggle downvote state: NONE/UPVOTE -> DOWNVOTE, DOWNVOTE -> NONE
     */
    fun toggleDownvote(): UserInteraction =
        copy(
            voteState = when (voteState) {
                VoteState.DOWNVOTE -> VoteState.NONE
                VoteState.UPVOTE, VoteState.NONE -> VoteState.DOWNVOTE
            },
            updatedAt = Clock.System.now(),
        )

    /**
     * Toggle bookmark state
     */
    fun toggleBookmark(): UserInteraction =
        copy(
            isBookmarked = !isBookmarked,
            updatedAt = Clock.System.now(),
        )

    /**
     * Remove all interactions (vote and bookmark)
     */
    fun clear(): UserInteraction =
        copy(
            voteState = VoteState.NONE,
            isBookmarked = false,
            updatedAt = Clock.System.now(),
        )

    companion object {
        /**
         * Create new user interaction with validation
         */
        fun create(
            itemId: String,
            itemType: ContentType,
            userId: UserId,
            voteState: VoteState = VoteState.NONE,
            isBookmarked: Boolean = false
        ): UserInteraction {
            val sanitizedId = generateId(itemId, userId.value)
            return UserInteraction(
                id = sanitizedId,
                itemId = itemId,
                itemType = itemType,
                userId = userId,
                voteState = voteState,
                isBookmarked = isBookmarked,
            )
        }

        /**
         * Generate consistent sanitized ID for Firestore document
         * Format: sanitized_itemId_sanitized_userId
         *
         * This must match the format used in Cloud Functions for consistency.
         * Reuses the same logic as the existing Vote.generateId() method.
         */
        fun generateId(
            itemId: String,
            userId: String
        ): String {
            val sanitizedItemId = itemId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val sanitizedUserId = userId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            return "${sanitizedItemId}_$sanitizedUserId"
        }

        /**
         * Parse document ID to extract itemId and userId
         * Returns Pair(itemId, userId) or null if invalid format
         */
        fun parseId(documentId: String): Pair<String, String>? {
            val parts = documentId.split("_")
            if (parts.size < 2) return null

            // Find the last underscore - everything before is itemId, after is userId
            val lastUnderscoreIndex = documentId.lastIndexOf("_")
            if (lastUnderscoreIndex == -1) return null

            val itemId = documentId.substring(0, lastUnderscoreIndex)
            val userId = documentId.substring(lastUnderscoreIndex + 1)

            return if (itemId.isNotBlank() && userId.isNotBlank()) {
                Pair(itemId, userId)
            } else {
                null
            }
        }
    }
}

/**
 * Statistics for user interactions on a specific item.
 * Used for displaying vote/bookmark counts.
 */
@Serializable
data class InteractionStats(
    val itemId: String,
    val upvoteCount: Int = 0,
    val downvoteCount: Int = 0,
    val bookmarkCount: Int = 0,
    val totalInteractions: Int = 0
)
