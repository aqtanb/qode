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
    val screenshotUrl: String? = null,
    val comments: List<String>? = null,

    // Timestamps - using current time as fallback
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
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
