package com.qodein.core.model

import kotlinx.serialization.Serializable

/**
 * Banner domain model representing promotional banners shown in the home feed.
 * Supports internationalization through country targeting and language arrays.
 */
@Serializable
data class Banner(
    val id: BannerId,
    val title: String,
    val description: String,
    val imageUrl: String,
    val targetCountries: List<String>, // ISO 3166-1 alpha-2 country codes (e.g., "KZ", "US", "GB") - empty means global
    val brandName: String,
    val ctaTitle: Map<String, String>, // CTA titles by language code ("default", "en", "kk", "ru")
    val ctaDescription: Map<String, String>, // CTA descriptions by language code ("default", "en", "kk", "ru")
    val ctaUrl: String?, // Optional deep link or web URL
    val isActive: Boolean,
    val priority: Int, // Higher numbers show first
    val createdAt: Long, // Unix timestamp
    val updatedAt: Long, // Unix timestamp
    val expiresAt: Long? = null // Optional expiration timestamp
) {
    /**
     * Checks if this banner should be shown in the specified country
     */
    fun isVisibleInCountry(countryCode: String): Boolean = targetCountries.isEmpty() || targetCountries.contains(countryCode.uppercase())

    /**
     * Checks if this banner is currently active and not expired based on server time
     */
    fun isDisplayable(currentServerTime: Long): Boolean = isActive && (expiresAt == null || expiresAt > currentServerTime)

    companion object {
        /**
         * Creates a fallback banner for loading/error states
         */
        fun createFallback(
            id: String = "fallback",
            brandName: String = "Qode",
            title: String = "Advertise with us",
            description: String = "Link with us"
        ): Banner =
            Banner(
                id = BannerId(id),
                title = title,
                description = description,
                imageUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755543893/gmail-background-xntgf4y7772j0g6i_bbgr2w.jpg",
                targetCountries = emptyList(), // Show in all countries
                brandName = brandName,
                ctaTitle = mapOf(
                    "default" to "Place your advertisement",
                    "en" to "Place your advertisement",
                    "kk" to "Жарнама орналастыру",
                    "ru" to "Разместить рекламу",
                ),
                ctaDescription = mapOf(
                    "default" to "Contact us",
                    "en" to "Contact us",
                    "kk" to "Бізге хабарласыңыз",
                    "ru" to "Связаться с нами",
                ),
                ctaUrl = "https://mail.google.com/" +
                    "mail/?view=cm&fs=1&to=qodeinhq@gmail.com&su=Advertisement%20Request&" +
                    "body=Hello,%20I%20would%20like%20to%20place%20an%20advertisement",
                isActive = true,
                priority = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                expiresAt = null,
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

/**
 * Extension functions for Banner translation based on user's language preference
 */

/**
 * Returns the CTA title in the specified language with fallback chain:
 * Requested language → English → Default
 */
fun Banner.getTranslatedCtaTitle(language: Language): String =
    when (language) {
        Language.ENGLISH -> ctaTitle["en"] ?: ctaTitle["default"] ?: ""
        Language.KAZAKH -> ctaTitle["kk"] ?: ctaTitle["en"] ?: ctaTitle["default"] ?: ""
        Language.RUSSIAN -> ctaTitle["ru"] ?: ctaTitle["en"] ?: ctaTitle["default"] ?: ""
    }

/**
 * Returns the CTA description in the specified language with fallback chain:
 * Requested language → English → Default
 */
fun Banner.getTranslatedCtaDescription(language: Language): String =
    when (language) {
        Language.ENGLISH -> ctaDescription["en"] ?: ctaDescription["default"] ?: ""
        Language.KAZAKH -> ctaDescription["kk"] ?: ctaDescription["en"] ?: ctaDescription["default"] ?: ""
        Language.RUSSIAN -> ctaDescription["ru"] ?: ctaDescription["en"] ?: ctaDescription["default"] ?: ""
    }
