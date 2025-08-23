package com.qodein.core.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

// ================================================================================================
// POST DTOs
// ================================================================================================

/**
 * Firestore document model for Post.
 */
data class PostDto(
    @DocumentId
    val id: String = "",

    @PropertyName("authorId")
    val authorId: String = "",

    @PropertyName("authorUsername")
    val authorUsername: String = "",

    @PropertyName("authorAvatarUrl")
    val authorAvatarUrl: String? = null,

    @PropertyName("authorCountry")
    val authorCountry: String? = null,

    @PropertyName("title")
    val title: String? = null,

    @PropertyName("content")
    val content: String = "",

    @PropertyName("imageUrls")
    val imageUrls: List<String> = emptyList(),

    @PropertyName("tags")
    val tags: List<Map<String, Any>> = emptyList(), // Tag objects as maps

    @PropertyName("upvotes")
    val upvotes: Int = 0,

    @PropertyName("downvotes")
    val downvotes: Int = 0,

    @PropertyName("shares")
    val shares: Int = 0,

    @PropertyName("createdAt")
    @ServerTimestamp
    val createdAt: Timestamp? = null,

    // User interaction flags (computed at query time)
    @PropertyName("isUpvotedByCurrentUser")
    val isUpvotedByCurrentUser: Boolean = false,

    @PropertyName("isDownvotedByCurrentUser")
    val isDownvotedByCurrentUser: Boolean = false,

    @PropertyName("isBookmarkedByCurrentUser")
    val isBookmarkedByCurrentUser: Boolean = false
) {
    // Required no-argument constructor for Firestore
    constructor() : this(
        id = "",
        authorId = "",
        authorUsername = "",
        authorAvatarUrl = null,
        authorCountry = null,
        title = null,
        content = "",
        imageUrls = emptyList(),
        tags = emptyList(),
        upvotes = 0,
        downvotes = 0,
        shares = 0,
        createdAt = null,
        isUpvotedByCurrentUser = false,
        isDownvotedByCurrentUser = false,
        isBookmarkedByCurrentUser = false,
    )
}

// ================================================================================================
// PROMO CODE DTOs
// ================================================================================================

/**
 * Firestore document model for PromoCode.
 * Uses Firestore annotations for proper serialization.
 */
data class PromoCodeDto(
    @DocumentId
    val id: String = "",
    val code: String = "",
    val serviceName: String = "",
    val category: String? = null,
    val title: String = "",
    val description: String? = null,
    val type: String = "", // "percentage", "fixed",

    // Type-specific fields (null for types that don't use them)
    val discountPercentage: Double? = null,
    val discountAmount: Double? = null,
    val minimumOrderAmount: Double = 0.0,

    val isFirstUserOnly: Boolean = false,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val views: Int = 0,
    val shares: Int = 0,
    val screenshotUrl: String? = null,
    val targetCountries: List<String> = emptyList(),
    val isVerified: Boolean = false,

    // Timestamps - using current time as fallback
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val createdBy: String? = null,

    // User interaction flags (computed at query time)
    val isUpvotedByCurrentUser: Boolean = false,
    val isDownvotedByCurrentUser: Boolean = false,
    val isBookmarkedByCurrentUser: Boolean = false
)

/**
 * Firestore document model for PromoCodeVote.
 */
data class PromoCodeVoteDto(
    @DocumentId
    val id: String = "",
    val promoCodeId: String = "",
    val userId: String = "",
    val isUpvote: Boolean = false,
    @ServerTimestamp
    val votedAt: Timestamp? = null
)

// ================================================================================================
// COMMENT DTOs
// ================================================================================================

/**
 * Firestore document model for Comment.
 * Stored in subcollections: /promocodes/{id}/comments/{commentId} or /posts/{id}/comments/{commentId}
 */
data class CommentDto(
    @DocumentId
    val id: String = "",

    @PropertyName("parentId")
    val parentId: String = "",

    @PropertyName("parentType")
    val parentType: String = "", // "PROMO_CODE" or "POST"

    @PropertyName("authorId")
    val authorId: String = "",

    @PropertyName("authorUsername")
    val authorUsername: String = "",

    @PropertyName("authorAvatarUrl")
    val authorAvatarUrl: String? = null,

    @PropertyName("authorCountry")
    val authorCountry: String? = null,

    @PropertyName("content")
    val content: String = "",

    @PropertyName("imageUrls")
    val imageUrls: List<String> = emptyList(),

    @PropertyName("upvotes")
    val upvotes: Int = 0,

    @PropertyName("downvotes")
    val downvotes: Int = 0,

    @PropertyName("createdAt")
    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @PropertyName("isUpvotedByCurrentUser")
    val isUpvotedByCurrentUser: Boolean = false,

    @PropertyName("isDownvotedByCurrentUser")
    val isDownvotedByCurrentUser: Boolean = false
) {
    // Required no-argument constructor for Firestore
    constructor() : this(
        id = "",
        parentId = "",
        parentType = "",
        authorId = "",
        authorUsername = "",
        authorAvatarUrl = null,
        authorCountry = null,
        content = "",
        imageUrls = emptyList(),
        upvotes = 0,
        downvotes = 0,
        createdAt = null,
        isUpvotedByCurrentUser = false,
        isDownvotedByCurrentUser = false,
    )
}

// ================================================================================================
// BANNER DTOs
// ================================================================================================

/**
 * Firestore document model for Promo.
 * User-submitted deals without actual promo codes.
 */
data class PromoDto(
    @DocumentId
    val id: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("description")
    val description: String = "",

    @PropertyName("imageUrls")
    val imageUrls: List<String> = emptyList(),

    @PropertyName("serviceName")
    val serviceName: String = "",

    @PropertyName("category")
    val category: String? = null,

    @PropertyName("targetCountries")
    val targetCountries: List<String> = emptyList(),

    @PropertyName("upvotes")
    val upvotes: Int = 0,

    @PropertyName("downvotes")
    val downvotes: Int = 0,

    @PropertyName("views")
    val views: Int = 0,

    @PropertyName("shares")
    val shares: Int = 0,

    @PropertyName("isVerified")
    val isVerified: Boolean = false,

    @PropertyName("createdBy")
    val createdBy: String = "",

    @PropertyName("createdAt")
    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @PropertyName("expiresAt")
    val expiresAt: Timestamp? = null,

    // User interaction flags (computed at query time)
    @PropertyName("isUpvotedByCurrentUser")
    val isUpvotedByCurrentUser: Boolean = false,

    @PropertyName("isDownvotedByCurrentUser")
    val isDownvotedByCurrentUser: Boolean = false,

    @PropertyName("isBookmarkedByCurrentUser")
    val isBookmarkedByCurrentUser: Boolean = false
) {
    // Required no-argument constructor for Firestore
    constructor() : this(
        id = "",
        title = "",
        description = "",
        imageUrls = emptyList(),
        serviceName = "",
        category = null,
        targetCountries = emptyList(),
        upvotes = 0,
        downvotes = 0,
        views = 0,
        shares = 0,
        isVerified = false,
        createdBy = "",
        createdAt = null,
        expiresAt = null,
        isUpvotedByCurrentUser = false,
        isDownvotedByCurrentUser = false,
        isBookmarkedByCurrentUser = false,
    )
}

// ================================================================================================
// BANNER DTOs
// ================================================================================================

/**
 * Data Transfer Object for Banner entities in Firestore.
 * Follows Firebase naming conventions and supports automatic serialization.
 */
data class BannerDto(
    @PropertyName("id")
    val id: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("description")
    val description: String = "",

    @PropertyName("imageUrl")
    val imageUrl: String = "",

    @PropertyName("targetCountries")
    val targetCountries: List<String> = emptyList(),

    @PropertyName("brandName")
    val brandName: String = "",

    @PropertyName("ctaTitle")
    val ctaTitle: Map<String, String> = emptyMap(),

    @PropertyName("ctaDescription")
    val ctaDescription: Map<String, String> = emptyMap(),

    @PropertyName("ctaUrl")
    val ctaUrl: String? = null,

    @PropertyName("isActive")
    val isActive: Boolean = true,

    @PropertyName("priority")
    val priority: Int = 0,

    @PropertyName("createdAt")
    val createdAt: Timestamp? = null,

    @PropertyName("updatedAt")
    val updatedAt: Timestamp? = null,

    @PropertyName("expiresAt")
    val expiresAt: Timestamp? = null

) {
    // Required no-argument constructor for Firestore
    constructor() : this(
        id = "",
        title = "",
        description = "",
        imageUrl = "",
        targetCountries = emptyList(),
        brandName = "",
        ctaTitle = emptyMap(),
        ctaDescription = emptyMap(),
        ctaUrl = null,
        isActive = true,
        priority = 0,
        createdAt = null,
        updatedAt = null,
        expiresAt = null,
    )
}
