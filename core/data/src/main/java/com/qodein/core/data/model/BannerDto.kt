package com.qodein.core.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Data Transfer Object for Banner entities in Firestore.
 * Follows Firebase naming conventions and supports automatic serialization.
 */
data class BannerDto(
    @PropertyName("id")
    val id: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("description")
    val description: String = "",

    @PropertyName("imageUrl")
    val imageUrl: String = "",

    @PropertyName("targetCountries")
    val targetCountries: List<String> = emptyList(),

    @PropertyName("brandName")
    val brandName: String = "",

    @PropertyName("ctaTitle")
    val ctaTitle: Map<String, String> = emptyMap(),

    @PropertyName("ctaDescription")
    val ctaDescription: Map<String, String> = emptyMap(),

    @PropertyName("ctaUrl")
    val ctaUrl: String? = null,

    @PropertyName("isActive")
    val isActive: Boolean = true,

    @PropertyName("priority")
    val priority: Int = 0,

    @PropertyName("createdAt")
    val createdAt: Timestamp? = null,

    @PropertyName("updatedAt")
    val updatedAt: Timestamp? = null,

    @PropertyName("expiresAt")
    val expiresAt: Timestamp? = null

) {
    // Required no-argument constructor for Firestore
    constructor() : this(
        id = "",
        title = "",
        description = "",
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
