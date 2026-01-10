package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * Service information embedded in promocode.
 * Denormalized for optimal read performance.
 */
data class PromocodeServiceDto(val id: String? = null, val name: String = "", val logoUrl: String? = null, val siteUrl: String? = null)

/**
 * Discount representation with support for percentage, fixed amount, and free items.
 */
data class PromocodeDiscountDto(val type: String? = null, val value: Double? = null, val freeItemDescription: String? = null) {
    companion object {
        const val TYPE_PERCENTAGE = "Percentage"
        const val TYPE_FIXED_AMOUNT = "FixedAmount"
        const val TYPE_FREE_ITEM = "FreeItem"
    }
}

/**
 * Engagement metrics for promocode voting.
 */
data class PromocodeEngagementDto(val upvotes: Int = 0, val downvotes: Int = 0, val voteScore: Int = 0)

/**
 * Author information embedded in promocode.
 * Denormalized for optimal read performance.
 */
data class PromocodeAuthorDto(val id: String = "", val username: String? = null, val avatarUrl: String? = null)

/**
 * Main promocode document stored in Firestore.
 * Grouped structure for better organization and maintainability.
 */
data class PromocodeDto(
    @DocumentId val documentId: String = "",
    val code: String = "",

    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),

    val service: PromocodeServiceDto = PromocodeServiceDto(),

    val discount: PromocodeDiscountDto = PromocodeDiscountDto(
        type = PromocodeDiscountDto.TYPE_PERCENTAGE,
        value = 0.0,
    ),
    val minimumOrderAmount: Double = 0.0,

    val description: String? = null,

    val engagement: PromocodeEngagementDto = PromocodeEngagementDto(),

    val author: PromocodeAuthorDto = PromocodeAuthorDto(),

    @PropertyName("verified")
    val isVerified: Boolean = false,

    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_NAME = "promocodes"

        const val FIELD_SERVICE_NAME = "service.name"
        const val FIELD_VOTE_SCORE = "engagement.voteScore"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_END_DATE = "endDate"
    }
}

data class PagedPromocodesDto(val items: List<PromocodeDto>, val lastDocument: DocumentSnapshot?, val hasMore: Boolean)
