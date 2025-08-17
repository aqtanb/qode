package com.qodein.core.model

import kotlinx.serialization.Serializable

/**
 * Banner domain model representing promotional banners shown in the home feed.
 * Supports internationalization through country targeting.
 */
@Serializable
data class Banner(
    val id: BannerId,
    val title: String,
    val description: String,
    val imageUrl: String,
    val targetCountries: List<String>, // ISO 3166-1 alpha-2 country codes (e.g., "KZ", "US", "GB")
    val brandName: String,
    val gradientColors: List<String>, // Hex color values for gradient background
    val ctaText: String, // Call-to-action button text
    val ctaUrl: String?, // Optional deep link or web URL
    val isActive: Boolean,
    val priority: Int, // Higher numbers show first
    val createdAt: Long, // Unix timestamp
    val updatedAt: Long // Unix timestamp
) {
    /**
     * Checks if this banner should be shown in the specified country
     */
    fun isVisibleInCountry(countryCode: String): Boolean = targetCountries.isEmpty() || targetCountries.contains(countryCode.uppercase())

    /**
     * Checks if this banner is currently active and should be displayed
     */
    val isDisplayable: Boolean
        get() = isActive

    companion object {
        /**
         * Creates a fallback banner for loading/error states
         */
        fun createFallback(
            id: String = "fallback",
            brandName: String = "Qode",
            title: String = "Welcome to Qode",
            description: String = "Discover amazing deals and offers"
        ): Banner =
            Banner(
                id = BannerId(id),
                title = title,
                description = description,
                imageUrl = "", // Empty for fallback
                targetCountries = emptyList(), // Show in all countries
                brandName = brandName,
                gradientColors = listOf("#6366F1", "#8B5CF6"), // Default gradient
                ctaText = "Explore Deals",
                ctaUrl = null,
                isActive = true,
                priority = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
    }
}

/**
 * Type-safe identifier for Banner entities
 */
@Serializable
@JvmInline
value class BannerId(val value: String) {
    init {
        require(value.isNotBlank()) { "BannerId cannot be blank" }
    }

    override fun toString(): String = value
}
