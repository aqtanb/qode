package com.qodein.shared.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

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
 * Represents a user's vote on content (promo codes, posts, or comments).
 * Stored in item's subcollection: /promocodes/{id}/votes/{userId} or /posts/{id}/votes/{userId}
 */
@Serializable
data class UserVote(
    val id: String, // Generated: userId_itemId
    val userId: UserId,
    val itemId: String, // PromoCodeId.value, PostId.value, or CommentId.value
    val itemType: VoteType,
    val isUpvote: Boolean, // true = upvote, false = downvote
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
            isUpvote: Boolean
        ): UserVote =
            UserVote(
                id = generateId(userId.value, itemId),
                userId = userId,
                itemId = itemId,
                itemType = itemType,
                isUpvote = isUpvote,
            )

        private fun generateId(
            userId: String,
            itemId: String
        ): String = "${userId}_$itemId"
    }
}

@Serializable
enum class VoteType {
    PROMO_CODE,
    POST,
    COMMENT
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
