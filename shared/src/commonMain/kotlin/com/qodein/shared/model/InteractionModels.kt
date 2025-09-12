
@file:UseContextualSerialization(Instant::class) // This line is conceptual, you typically add @Contextual per property

package com.qodein.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Represents a user's bookmark on content (promo codes or posts).
 * Stored in user's subcollection: /users/{userId}/bookmarks/{itemId}
 */
@Serializable
data class UserBookmark(
    val id: String, // Generated: userId_itemId
    val userId: UserId,
    val itemId: String, // PromoCodeId.value or PostId.value
    val itemType: BookmarkType,
    val itemTitle: String, // Denormalized for quick display
    val itemCategory: String? = null, // Denormalized for filtering
    val createdAt: Instant = Clock.System.now()
) {
    init {
        require(itemId.isNotBlank()) { "Item ID cannot be blank" }
        require(itemTitle.isNotBlank()) { "Item title cannot be blank" }
    }

    companion object {
        fun create(
            userId: UserId,
            itemId: String,
            itemType: BookmarkType,
            itemTitle: String,
            itemCategory: String? = null
        ): UserBookmark =
            UserBookmark(
                id = generateId(userId.value, itemId),
                userId = userId,
                itemId = itemId,
                itemType = itemType,
                itemTitle = itemTitle.trim(),
                itemCategory = itemCategory?.trim(),
            )

        private fun generateId(
            userId: String,
            itemId: String
        ): String = "${userId}_$itemId"
    }
}

@Serializable
enum class BookmarkType {
    PROMO_CODE,
    POST
}

/**
 * Unified vote model for all content types (promo codes, posts, comments, promos).
 * Stored in unified collection: /votes/{sanitized_itemId}_{sanitized_userId}
 */
@Serializable
data class Vote(
    val id: String, // Generated: sanitized_itemId_sanitized_userId
    val userId: UserId,
    val itemId: String, // ID of the content being voted on
    val itemType: VoteType,
    val voteState: VoteState,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
) {
    init {
        require(itemId.isNotBlank()) { "Item ID cannot be blank" }
    }

    companion object {
        fun create(
            userId: UserId,
            itemId: String,
            itemType: VoteType,
            voteState: VoteState
        ): Vote =
            Vote(
                id = generateId(itemId, userId.value),
                userId = userId,
                itemId = itemId,
                itemType = itemType,
                voteState = voteState,
            )

        private fun generateId(
            itemId: String,
            userId: String
        ): String {
            val sanitizedItemId = itemId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val sanitizedUserId = userId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            return "${sanitizedItemId}_$sanitizedUserId"
        }
    }

    /**
     * Toggle upvote: NONE/DOWNVOTE -> UPVOTE, UPVOTE -> NONE
     */
    fun toggleUpvote(): VoteState =
        when (voteState) {
            VoteState.UPVOTE -> VoteState.NONE
            VoteState.DOWNVOTE, VoteState.NONE -> VoteState.UPVOTE
        }

    /**
     * Toggle downvote: NONE/UPVOTE -> DOWNVOTE, DOWNVOTE -> NONE
     */
    fun toggleDownvote(): VoteState =
        when (voteState) {
            VoteState.DOWNVOTE -> VoteState.NONE
            VoteState.UPVOTE, VoteState.NONE -> VoteState.DOWNVOTE
        }

    /**
     * Remove vote: Any state -> NONE
     */
    fun remove(): VoteState = VoteState.NONE
}

@Serializable
enum class VoteState {
    UPVOTE, // User has upvoted
    DOWNVOTE, // User has downvoted
    NONE // User has no vote (removed or never voted)
}

@Serializable
enum class VoteType {
    PROMO_CODE,
    POST,
    COMMENT,
    PROMO
}

/**
 * Tracks user activity for feed generation and analytics.
 * Stored in global collection: /user_activities/{activityId}
 */
@Serializable
data class UserActivity(
    val id: String,
    val userId: UserId,
    val type: ActivityType,
    val targetId: String, // ID of the content affected
    val targetType: String, // "promo_code", "post", "comment"
    val targetTitle: String? = null, // Denormalized for display
    val metadata: Map<String, String> = emptyMap(), // Additional context
    val createdAt: Instant = Clock.System.now()
) {
    init {
        require(targetId.isNotBlank()) { "Target ID cannot be blank" }
        require(targetType.isNotBlank()) { "Target type cannot be blank" }
    }

    companion object {
        fun create(
            userId: UserId,
            type: ActivityType,
            targetId: String,
            targetType: String,
            targetTitle: String? = null,
            metadata: Map<String, String> = emptyMap()
        ): UserActivity =
            UserActivity(
                id = generateId(),
                userId = userId,
                type = type,
                targetId = targetId,
                targetType = targetType,
                targetTitle = targetTitle?.trim(),
                metadata = metadata,
            )

        private fun generateId(): String = "activity_${Clock.System.now().toEpochMilliseconds()}_${(0..999).random()}"
    }
}

@Serializable
enum class ActivityType {
    CREATED_PROMO_CODE,
    CREATED_POST,
    COMMENTED,
    UPVOTED,
    DOWNVOTED,
    BOOKMARKED,
    SHARED,
    VIEWED_PROMO_CODE
}
