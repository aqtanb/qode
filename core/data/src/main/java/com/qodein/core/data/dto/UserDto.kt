package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Firestore DTO for user profile data.
 * Embedded inside UserDto as a nested field.
 */
data class UserProfileDto(val displayName: String = "", val photoUrl: String? = null)

/**
 * Firestore DTO for user statistics.
 * Embedded inside UserDto as a nested field.
 */
data class UserStatsDto(val submittedPromocodesCount: Int = 0, val submittedPostsCount: Int = 0)

/**
 * Firestore DTO for user legal consent tracking.
 * Embedded inside UserDto as a nested field.
 */
data class UserConsentDto(val legalPoliciesAcceptedAt: Timestamp? = null)

/**
 * Firestore DTO for complete user document.
 * Maps to the "users/{userId}" collection.
 */
data class UserDto(
    @DocumentId
    val documentId: String = "",
    val email: String = "",
    val profile: UserProfileDto = UserProfileDto(),
    val stats: UserStatsDto = UserStatsDto(),
    val consent: UserConsentDto = UserConsentDto()
) {
    companion object {
        const val COLLECTION_NAME = "users"
    }
}
