package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.VoteState

/**
 * Firestore DTO for unified user interactions (votes + bookmarks).
 * Stored in: /user_interactions/{itemId}_{userId}
 *
 * This replaces separate VoteDto and UserBookmarkDto for cost efficiency.
 */
data class UserInteractionDto(
    @DocumentId
    val documentId: String = "", // Generated: sanitized_itemId_sanitized_userId
    val itemId: String = "", // ID of the content (promocode, post, comment)
    val itemType: String = "", // ContentType as string for Firestore
    val userId: String = "", // User performing the interaction
    val voteState: String = "NONE", // VoteState as string
    @PropertyName("bookmarked")
    val isBookmarked: Boolean = false, // Whether user has bookmarked this content
    @ServerTimestamp
    val createdAt: Timestamp = Timestamp.now(),
    @ServerTimestamp
    val updatedAt: Timestamp = Timestamp.now()
) {
    companion object {
        /**
         * Create DTO with proper validation and defaults
         */
        fun create(
            itemId: String,
            itemType: ContentType,
            userId: String,
            voteState: VoteState = VoteState.NONE,
            isBookmarked: Boolean = false
        ): UserInteractionDto {
            val sanitizedId = generateId(itemId, userId)
            return UserInteractionDto(
                documentId = sanitizedId,
                itemId = itemId,
                itemType = itemType.name,
                userId = userId,
                voteState = voteState.name,
                isBookmarked = isBookmarked,
            )
        }

        /**
         * Generate consistent sanitized ID for Firestore document
         * Must match the format used in UserInteraction.generateId()
         */
        private fun generateId(
            itemId: String,
            userId: String
        ): String {
            val sanitizedItemId = itemId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val sanitizedUserId = userId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            return "${sanitizedItemId}_$sanitizedUserId"
        }
    }
}
