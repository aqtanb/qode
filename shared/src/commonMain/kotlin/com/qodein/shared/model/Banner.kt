package com.qodein.shared.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class BannerId(val value: String) {
    init {
        require(value.isNotBlank()) { "BannerId cannot be blank" }
    }

    override fun toString(): String = value
}

@Serializable
data class Banner(
    val id: BannerId,
    val imageUrl: String,
    val ctaTitle: Map<String, String>, // CTA titles by language code ("default", "en", "kk", "ru")
    val ctaDescription: Map<String, String>, // CTA descriptions by language code ("default", "en", "kk", "ru")
    val ctaUrl: String?,
    val priority: Int // Higher numbers show first
)

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
