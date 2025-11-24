package com.qodein.core.data.dto

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Firestore DTO for user profile data.
 * Embedded inside UserDto as a nested field.
 */
data class UserProfileDto(val displayName: String = "", val photoUrl: String? = null)

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
