package com.qodein.core.data.dto

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Firestore DTO for unified user interactions (votes + bookmarks).
 * Stored in: /users/{userId}/interactions/{itemType}_{itemId}
 */
data class UserInteractionDto(
    @DocumentId
    val documentId: String = "",
    val itemId: String = "",
    val itemType: String = "",
    val userId: String = "",
    val voteState: String = "NONE",
    @PropertyName("bookmarked")
    val isBookmarked: Boolean = false
)
