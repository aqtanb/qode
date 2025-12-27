package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * Discount representation for Firestore with discriminator.
 * Plain data class to satisfy Firestore's no-arg constructor requirement.
 */
data class DiscountDto(val type: String? = null, val value: Double? = null) {
    companion object {
        const val TYPE_PERCENTAGE = "Percentage"
        const val TYPE_FIXED_AMOUNT = "FixedAmount"
    }
}

data class PromocodeDto(
    @DocumentId val documentId: String = "",
    val code: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val serviceName: String = "",
    val discount: DiscountDto = DiscountDto(type = DiscountDto.TYPE_PERCENTAGE, value = 0.0),
    val minimumOrderAmount: Double = 0.0,

    val description: String? = null,

    val serviceId: String? = null,
    val serviceLogoUrl: String? = null,

    @PropertyName("firstUserOnly")
    val isFirstUserOnly: Boolean = false,
    @PropertyName("oneTimeUseOnly")
    val isOneTimeUseOnly: Boolean = false,
    @PropertyName("verified")
    val isVerified: Boolean = false,

    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val voteScore: Int = 0,

    val authorId: String = "",
    val authorUsername: String? = null,
    val authorAvatarUrl: String? = null,

    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_NAME = "promocodes"
        const val FIELD_SERVICE_NAME = "serviceName"
        const val FIELD_VOTE_SCORE = "voteScore"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_END_DATE = "endDate"
    }
}

data class PagedPromocodesDto(val items: List<PromocodeDto>, val lastDocument: DocumentSnapshot?, val hasMore: Boolean)
