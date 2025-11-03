package com.qodein.core.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

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
    val category: String = "Unspecified",
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
    @get:PropertyName("isOneTimeUseOnly")
    @JvmField
    val isOneTimeUseOnly: Boolean = false,

    // Engagement metrics
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val voteScore: Int = 0, // Computed by Cloud Function: upvotes - downvotes
    val shares: Int = 0,
    val comments: Int? = null,

    // Media and verification
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
    val createdBy: String = "",
    val createdByUsername: String? = null,
    val createdByAvatarUrl: String? = null,
    val serviceLogoUrl: String? = null

) {
    // Required no-argument constructor for Firestore
    constructor() : this(
        documentId = "",
        code = "",
        serviceId = null,
        serviceName = "",
        category = "Unspecified",
        description = null,
        type = "",
        discountPercentage = null,
        discountAmount = null,
        minimumOrderAmount = 0.0,
        isFirstUserOnly = false,
        isOneTimeUseOnly = false,
        upvotes = 0,
        downvotes = 0,
        voteScore = 0,
        shares = 0,
        comments = null,
        targetCountries = emptyList(),
        isVerified = false,
        startDate = Timestamp.now(),
        endDate = Timestamp.now(),
        createdAt = null,
        updatedAt = null,
        createdBy = "",
        createdByUsername = null,
        createdByAvatarUrl = null,
        serviceLogoUrl = null,
    )
}

/*
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
