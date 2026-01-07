package com.qodein.core.data.dto

import com.google.firebase.firestore.DocumentId

/**
 * Firestore document model for Service.
 * Represents services/brands that offer promocodes.
 */
data class ServiceDto(
    @DocumentId
    val documentId: String = "",
    val name: String = "",
    val siteUrl: String = "",
    val logoUrl: String? = null,
    val promoCodeCount: Int = 0
) {
    companion object {
        const val COLLECTION_NAME = "services"
        const val FIELD_NAME = "name"
        const val FIELD_PROMO_CODE_COUNT = "promoCodeCount"
    }
}
