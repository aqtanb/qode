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
    val createdAt: Timestamp? = null
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
    val documentId: String = "",

    // Document data fields (no id field - Firebase provides document ID)
    val code: String = "",
    val serviceId: String? = null, // Reference to Service document ID
    val serviceName: String = "", // Denormalized for display and filtering
    val category: String? = null,
    val title: String = "",
    val description: String? = null,
    val type: String = "", // "percentage", "fixed"

    // Type-specific fields (null for types that don't use them)
    val discountPercentage: Double? = null,
    val discountAmount: Double? = null,
    val minimumOrderAmount: Double = 0.0,

    // User-specific flags (handle both field name variations from Firestore)
    @get:PropertyName("isFirstUserOnly")
    @JvmField
    val isFirstUserOnly: Boolean = false,

    // Engagement metrics
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val voteScore: Int = 0, // Computed by Cloud Function: upvotes - downvotes
    val views: Int = 0,
    val shares: Int = 0,
    val comments: Int? = null,

    // Media and verification
    val screenshotUrl: String? = null,
    val targetCountries: List<String> = emptyList(),
    @get:PropertyName("isVerified")
    @JvmField
    val isVerified: Boolean = false,

    // Timestamps
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val createdBy: String? = null

) {
    // Required no-argument constructor for Firestore
    constructor() : this(
        documentId = "",
        code = "",
        serviceId = null,
        serviceName = "",
        category = null,
        title = "",
        description = null,
        type = "",
        discountPercentage = null,
        discountAmount = null,
        minimumOrderAmount = 0.0,
        isFirstUserOnly = false,
        upvotes = 0,
        downvotes = 0,
        voteScore = 0,
        views = 0,
        shares = 0,
        comments = null,
        screenshotUrl = null,
        targetCountries = emptyList(),
        isVerified = false,
        startDate = Timestamp.now(),
        endDate = Timestamp.now(),
        createdAt = null,
        updatedAt = null,
        createdBy = null,
    )
}
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
    val createdAt: Timestamp? = null
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
    val expiresAt: Timestamp? = null
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
    @DocumentId
    val documentId: String = "",
    val imageUrl: String = "",
    val targetCountries: List<String> = emptyList(),
    val brandName: String = "",
    val ctaTitle: Map<String, String> = emptyMap(),
    val ctaDescription: Map<String, String> = emptyMap(),
    val ctaUrl: String? = null,
    @get:PropertyName("isActive")
    val isActive: Boolean = true,
    val priority: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val expiresAt: Timestamp? = null

) {
    // Required no-argument constructor for Firestore
    constructor() : this(
        documentId = "",
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
