package com.qodein.core.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

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
    val createdAt: Timestamp? = null,

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
    val submittedPostsCount: Int = 0,

    @PropertyName("createdAt")
    val createdAt: Timestamp? = null
) {
    constructor() : this(
        submittedPromocodesCount = 0,
        submittedPostsCount = 0,
        createdAt = null,
    )
}

/**
 * Firestore DTO for complete user document.
 * Maps to the "users/{userId}" collection.
 */
data class UserDto(
    @PropertyName("email")
    val email: String = "",

    @PropertyName("profile")
    val profile: UserProfileDto = UserProfileDto(),

    @PropertyName("stats")
    val stats: UserStatsDto = UserStatsDto(),

    @PropertyName("country")
    val country: String? = null
) {
    constructor() : this(
        email = "",
        profile = UserProfileDto(),
        stats = UserStatsDto(),
        country = null,
    )
}
