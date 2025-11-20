package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore DTO for user profile data.
 * Embedded inside UserDto as a nested field.
 */
data class UserProfileDto(
    @PropertyName("firstName")
    val firstName: String = "",

    @PropertyName("lastName")
    val lastName: String? = null,

    @PropertyName("bio")
    val bio: String? = null,

    @PropertyName("photoUrl")
    val photoUrl: String? = null,

    @PropertyName("createdAt")
    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @ServerTimestamp
    @PropertyName("updatedAt")
    val updatedAt: Timestamp? = null
) {
    constructor() : this(
        firstName = "",
        lastName = null,
        bio = null,
        photoUrl = null,
        createdAt = null,
        updatedAt = null,
    )
}

/**
 * Firestore DTO for user statistics.
 * Embedded inside UserDto as a nested field.
 */
data class UserStatsDto(
    @PropertyName("submittedPromocodesCount")
    val submittedPromocodesCount: Int = 0,

    @PropertyName("submittedPostsCount")
    val submittedPostsCount: Int = 0
) {
    companion object {
        const val FIELD_SUBMITTED_PROMOCODES_COUNT = "submittedPromocodesCount"
        const val FIELD_SUBMITTED_POSTS_COUNT = "submittedPostsCount"
    }
}

/**
 * Firestore DTO for complete user document.
 * Maps to the "users/{userId}" collection.
 */
data class UserDto(
    @DocumentId
    val documentId: String = "",
    @PropertyName("email")
    val email: String = "",

    @PropertyName("profile")
    val profile: UserProfileDto = UserProfileDto(),

    @PropertyName("stats")
    val stats: UserStatsDto = UserStatsDto()
) {
    companion object {
        const val COLLECTION_NAME = "users"
        const val FIELD_EMAIL = "email"
        const val FIELD_PROFILE = "profile"
        const val FIELD_STATS = "stats"
    }
}
