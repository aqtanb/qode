package com.qodein.core.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

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
    val title: String? = null,
    val description: String? = null,
    val type: String = "", // "percentage", "fixed", "promo"

    // Type-specific fields (null for types that don't use them)
    val discountPercentage: Double? = null,
    val discountAmount: Double? = null,
    val minimumOrderAmount: Double? = null,
    val maximumDiscount: Double? = null,

    // Common fields
    val usageLimit: Int? = null,
    val isFirstUserOnly: Boolean = false,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val views: Int = 0,
    val screenshotUrl: String? = null,
    val comments: List<String>? = null,

    // Timestamps
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val createdBy: String? = null
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

/**
 * Firestore document model for PromoCodeUsage.
 */
data class PromoCodeUsageDto(
    @DocumentId
    val id: String = "",
    val promoCodeId: String = "",
    val userId: String = "",
    val orderAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    @ServerTimestamp
    val usedAt: Timestamp? = null
)
