package com.qodein.core.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

// ================================================================================================
// USER INTERACTION DTOs
// ================================================================================================

/**
 * Firestore document model for UserBookmark.
 * Stored in user subcollections: /users/{userId}/bookmarks/{itemId}
 */
data class UserBookmarkDto(
    @DocumentId
    val id: String = "",

    @PropertyName("userId")
    val userId: String = "",

    @PropertyName("itemId")
    val itemId: String = "",

    @PropertyName("itemType")
    val itemType: String = "", // "PROMO_CODE" or "POST"

    @PropertyName("itemTitle")
    val itemTitle: String = "",

    @PropertyName("itemCategory")
    val itemCategory: String? = null,

    @PropertyName("createdAt")
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    // Required no-argument constructor for Firestore
    constructor() : this(
        id = "",
        userId = "",
        itemId = "",
        itemType = "",
        itemTitle = "",
        itemCategory = null,
        createdAt = null,
    )
}

/**
 * Firestore document model for UserVote.
 * Stored in item subcollections: /promocodes/{id}/votes/{userId}, /posts/{id}/votes/{userId}, /comments/{id}/votes/{userId}
 */
data class UserVoteDto(
    @DocumentId
    val id: String = "",

    @PropertyName("userId")
    val userId: String = "",

    @PropertyName("itemId")
    val itemId: String = "",

    @PropertyName("itemType")
    val itemType: String = "", // "PROMO_CODE", "POST", or "COMMENT"

    @PropertyName("isUpvote")
    val isUpvote: Boolean = false,

    @PropertyName("createdAt")
    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @PropertyName("updatedAt")
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    // Required no-argument constructor for Firestore
    constructor() : this(
        id = "",
        userId = "",
        itemId = "",
        itemType = "",
        isUpvote = false,
        createdAt = null,
        updatedAt = null,
    )
}

/**
 * Firestore document model for UserActivity.
 * Stored in global collection: /user_activities/{activityId}
 */
data class UserActivityDto(
    @DocumentId
    val id: String = "",

    @PropertyName("userId")
    val userId: String = "",

    @PropertyName("type")
    val type: String = "", // ActivityType enum values

    @PropertyName("targetId")
    val targetId: String = "",

    @PropertyName("targetType")
    val targetType: String = "",

    @PropertyName("targetTitle")
    val targetTitle: String? = null,

    @PropertyName("metadata")
    val metadata: Map<String, String> = emptyMap(),

    @PropertyName("createdAt")
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    // Required no-argument constructor for Firestore
    constructor() : this(
        id = "",
        userId = "",
        type = "",
        targetId = "",
        targetType = "",
        targetTitle = null,
        metadata = emptyMap(),
        createdAt = null,
    )
}

// ================================================================================================
// SERVICE DTOs
// ================================================================================================

/**
 * Firestore document model for Service.
 * Represents services/brands that offer promo codes.
 */
data class ServiceDto(
    @DocumentId
    val documentId: String = "",

    @PropertyName("name")
    val name: String = "",

    @PropertyName("category")
    val category: String = "",

    @PropertyName("logoUrl")
    val logoUrl: String? = null,

    @PropertyName("isPopular")
    val isPopular: Boolean = false,

    @PropertyName("promoCodeCount")
    val promoCodeCount: Int = 0,

    @PropertyName("createdAt")
    val createdAt: Timestamp? = null
) {
    // No-args constructor required for Firestore deserialization
    constructor() : this("", "", "", null, false, 0, null)
}
