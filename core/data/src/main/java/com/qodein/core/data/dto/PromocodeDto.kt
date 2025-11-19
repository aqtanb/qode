package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * Polymorphic discount representation for Firestore.
 * Stored with discriminator field @type to distinguish between percentage and fixed amount.
 */
sealed interface DiscountDto {
    val value: Double

    data class Percentage(
        @get:PropertyName("value")
        override val value: Double
    ) : DiscountDto {
        @get:PropertyName("@type")
        val type: String = TYPE

        companion object {
            const val TYPE = "Percentage"
        }
    }

    data class FixedAmount(
        @get:PropertyName("value")
        override val value: Double
    ) : DiscountDto {
        @get:PropertyName("@type")
        val type: String = TYPE

        companion object {
            const val TYPE = "FixedAmount"
        }
    }
}

data class PromocodeDto(
    @DocumentId val documentId: String = "",
    val code: String,
    val startDate: Timestamp,
    val endDate: Timestamp,
    val serviceName: String,
    val discount: DiscountDto,
    val minimumOrderAmount: Double,

    val description: String? = null,

    val serviceId: String? = null,
    val serviceLogoUrl: String? = null,

    val isFirstUserOnly: Boolean = false,
    val isOneTimeUseOnly: Boolean = false,
    val isVerified: Boolean = false,

    val upvotes: Int = 0,
    val downvotes: Int = 0,

    val authorId: String,
    val authorUsername: String? = null,
    val authorAvatarUrl: String? = null,

    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_NAME = "promocodes"
        const val FIELD_CODE = "code"
        const val FIELD_SERVICE_ID = "serviceId"
        const val FIELD_SERVICE_NAME = "serviceName"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_DISCOUNT = "discount"
        const val FIELD_MINIMUM_ORDER_AMOUNT = "minimumOrderAmount"
        const val FIELD_IS_FIRST_USER_ONLY = "isFirstUserOnly"
        const val FIELD_UPVOTES = "upvotes"
        const val FIELD_DOWNVOTES = "downvotes"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_END_DATE = "endDate"
    }
}

data class PagedPromocodesDto(val items: List<PromocodeDto>, val lastDocument: DocumentSnapshot?, val hasMore: Boolean)
