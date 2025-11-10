package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class BannerDto(
    @DocumentId
    val documentId: String = "",
    val imageUrl: String = "",
    val ctaTitle: Map<String, String> = emptyMap(),
    val ctaDescription: Map<String, String> = emptyMap(),
    val ctaUrl: String? = null,
    val isActive: Boolean = true,
    val priority: Int = 0,
    val expiresAt: Timestamp = Timestamp(253402300799, 0)
) {
    companion object {
        const val COLLECTION_NAME = "banners"
        const val FIELD_PRIORITY = "priority"
        const val FIELD_IS_ACTIVE = "isActive"
        const val FIELD_EXPIRES_AT = "expiresAt"
    }
}
