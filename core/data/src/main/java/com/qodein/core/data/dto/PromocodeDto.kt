package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class PromocodeDto(
    @DocumentId val documentId: String = "",
    val code: String,
    val startDate: Timestamp,
    val endDate: Timestamp,
    val serviceName: String,
    val type: String,
    val minimumOrderAmount: Double,

    val description: String? = null,
    val discountPercentage: Double? = null,
    val discountAmount: Double? = null,

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
    val updatedAt: Timestamp? = null,
) {
    companion object {
        const val COLLECTION_NAME = "promocodes"
        const val FIELD_NAME = "code"
        const val FIELD_SERVICE_ID = "serviceId"
        const val FIELD_SERVICE_NAME = "serviceName"
        const val FIELD_CATEGORY = "category"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_TYPE = "type"
        const val FIELD_DISCOUNT_PERCENTAGE = "discountPercentage"
        const val FIELD_DISCOUNT_AMOUNT = "discountAmount"
        const val FIELD_MINIMUM_ORDER_AMOUNT = "minimumOrderAmount"
        const val FIELD_IS_FIRST_USER_ONLY = "isFirstUserOnly"
    }
}
