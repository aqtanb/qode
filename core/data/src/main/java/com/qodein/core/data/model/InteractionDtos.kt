package com.qodein.core.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

// MARK: ffdssdf

/**
 * Firestore document model for Service.
 * Represents services/brands that offer promo codes.
 */
data class ServiceDto(
    @DocumentId
    val documentId: String = "",

    @PropertyName("name")
    val name: String = "",

    @PropertyName("category")
    val category: String = "",

    @PropertyName("logoUrl")
    val logoUrl: String? = null,

    @get:PropertyName("isPopular")
    val isPopular: Boolean = false,

    @PropertyName("promoCodeCount")
    val promoCodeCount: Int = 0,

    @PropertyName("createdAt")
    val createdAt: Timestamp? = null,

    // Add these missing fields
    @PropertyName("updatedAt")
    val updatedAt: Timestamp? = null,

    @PropertyName("domain")
    val domain: String? = null,

    @PropertyName("countsUpdatedAt")
    val countsUpdatedAt: Timestamp? = null
) {
    // No-args constructor required for Firestore deserialization
    constructor() : this("", "", "", null, false, 0, null, null, null, null)
}
