package com.qodein.shared.domain.provider

/**
 * Contract for providing localized strings for share functionality
 *
 * Domain layer defines WHAT it needs
 * Platform layer implements HOW to get localized strings
 */
interface ShareStringProvider {
    /**
     * Get formatted share text for a post
     * Example: "Check out this post: {title}"
     */
    fun getPostShareHeader(postTitle: String): String

    /**
     * Get author attribution text
     * Example: "By {authorName}"
     */
    fun getAuthorAttribution(authorName: String): String

    /**
     * Get call-to-action text
     * Example: "Read more on Qodein"
     */
    fun getPostShareCallToAction(): String

    /**
     * Get formatted share text for a promocode
     */
    fun getPromocodeShareHeader(
        serviceName: String,
        discount: String
    ): String

    /**
     * Get promocode label text
     * Example: "Code: {code}"
     */
    fun getPromocodeLabel(code: String): String
}
