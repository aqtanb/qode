@file:UseContextualSerialization(Instant::class) // This line is conceptual, you typically add @Contextual per property

package com.qodein.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant

// ================================================================================================
// PROMO CODE MODELS
// ================================================================================================

@Serializable
@JvmInline
value class PromoCodeId(val value: String) {
    init {
        require(value.isNotBlank()) { "PromoCode ID cannot be blank" }
    }

    override fun toString(): String = value
}

@Serializable
sealed class PromoCode {
    abstract val id: PromoCodeId // shouldnt we use composite document id lowercased brandname_promocode sanitized
    abstract val code: String
    abstract val serviceId: ServiceId? // Reference to Service document
    abstract val serviceName: String // Denormalized for display and filtering
    abstract val category: String
    abstract val description: String?
    abstract val minimumOrderAmount: Double
    abstract val startDate: Instant
    abstract val endDate: Instant
    abstract val isFirstUserOnly: Boolean
    abstract val isOneTimeUseOnly: Boolean
    abstract val upvotes: Int
    abstract val downvotes: Int
    abstract val shares: Int
    abstract val targetCountries: List<String>
    abstract val isVerified: Boolean
    abstract val createdAt: Instant
    abstract val createdBy: UserId
    abstract val createdByUsername: String? // Denormalized from User for display performance
    abstract val createdByAvatarUrl: String? // Denormalized from User for display performance
    abstract val serviceLogoUrl: String? // Denormalized from Service for display performance

    val isExpired: Boolean get() = Clock.System.now() > endDate
    val isNotStarted: Boolean get() = Clock.System.now() < startDate
    val isValidNow: Boolean get() = !isExpired && !isNotStarted
    val voteScore: Int get() = upvotes - downvotes

    abstract fun calculateDiscount(orderAmount: Double): Double

    @Serializable
    data class PercentagePromoCode(
        override val id: PromoCodeId,
        override val code: String,
        override val serviceId: ServiceId? = null,
        override val serviceName: String,
        override val category: String = "Unspecified",
        override val description: String? = null,
        val discountPercentage: Double,
        override val minimumOrderAmount: Double,
        override val startDate: Instant,
        override val endDate: Instant,
        override val isFirstUserOnly: Boolean = false,
        override val isOneTimeUseOnly: Boolean = false,
        override val upvotes: Int = 0,
        override val downvotes: Int = 0,
        override val shares: Int = 0,
        override val targetCountries: List<String> = emptyList(),
        override val isVerified: Boolean = false,
        override val createdAt: Instant = Clock.System.now(),
        override val createdBy: UserId,
        override val createdByUsername: String? = null,
        override val createdByAvatarUrl: String? = null,
        override val serviceLogoUrl: String? = null
    ) : PromoCode() {
        init {
            require(code.isNotBlank()) { "PromoCode code cannot be blank" }
            require(serviceName.isNotBlank()) { "Service name cannot be blank" }
            require(discountPercentage > 0 && discountPercentage <= 100) { "Discount percentage must be between 0 and 100" }
            require(minimumOrderAmount > 0) { "Minimum order amount must be positive" }
            require(upvotes >= 0) { "Upvotes cannot be negative" }
            require(downvotes >= 0) { "Downvotes cannot be negative" }
            require(shares >= 0) { "Shares cannot be negative" }
            require(endDate > startDate) { "End date must be after start date" }
        }

        override fun calculateDiscount(orderAmount: Double): Double = discountPercentage
    }

    @Serializable
    data class FixedAmountPromoCode(
        override val id: PromoCodeId,
        override val code: String,
        override val serviceId: ServiceId? = null,
        override val serviceName: String,
        override val category: String = "Unspecified",
        override val description: String? = null,
        val discountAmount: Double,
        override val minimumOrderAmount: Double,
        override val startDate: Instant,
        override val endDate: Instant,
        override val isFirstUserOnly: Boolean = false,
        override val isOneTimeUseOnly: Boolean = false,
        override val upvotes: Int = 0,
        override val downvotes: Int = 0,
        override val shares: Int = 0,
        override val targetCountries: List<String> = emptyList(),
        override val isVerified: Boolean = false,
        override val createdAt: Instant = Clock.System.now(),
        override val createdBy: UserId,
        override val createdByUsername: String? = null,
        override val createdByAvatarUrl: String? = null,
        override val serviceLogoUrl: String? = null
    ) : PromoCode() {
        init {
            require(code.isNotBlank()) { "PromoCode code cannot be blank" }
            require(serviceName.isNotBlank()) { "Service name cannot be blank" }
            require(discountAmount > 0) { "Discount amount must be positive" }
            require(minimumOrderAmount > 0) { "Minimum order amount must be positive" }
            require(upvotes >= 0) { "Upvotes cannot be negative" }
            require(downvotes >= 0) { "Downvotes cannot be negative" }
            require(shares >= 0) { "Shares cannot be negative" }
            require(endDate > startDate) { "End date must be after start date" }
        }

        override fun calculateDiscount(orderAmount: Double): Double = discountAmount * 100 / orderAmount
    }

    companion object {
        fun generateCompositeId(
            code: String,
            serviceName: String
        ): String {
            val cleanCode = code.lowercase().trim().replace(Regex("\\s+"), "_")
            val cleanService = serviceName.lowercase().trim().replace(Regex("\\s+"), "_")
            return "${cleanService}_$cleanCode"
        }

        fun createPercentage(
            code: String,
            serviceName: String,
            serviceId: ServiceId? = null,
            discountPercentage: Double,
            category: String = "Unspecified",
            description: String? = null,
            minimumOrderAmount: Double,
            startDate: Instant,
            endDate: Instant,
            isFirstUserOnly: Boolean = false,
            targetCountries: List<String> = emptyList(),
            createdBy: UserId,
            createdByUsername: String? = null,
            createdByAvatarUrl: String? = null,
            serviceLogoUrl: String? = null
        ): Result<PercentagePromoCode> =
            runCatching {
                val cleanCode = code.uppercase().trim()
                val cleanServiceName = serviceName.trim()
                PercentagePromoCode(
                    id = PromoCodeId(generateCompositeId(cleanCode, cleanServiceName)),
                    code = cleanCode,
                    serviceName = cleanServiceName,
                    serviceId = serviceId,
                    category = category.trim(),
                    description = description?.trim(),
                    discountPercentage = discountPercentage,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
                    isFirstUserOnly = isFirstUserOnly,
                    targetCountries = targetCountries.map { it.uppercase() },
                    createdBy = createdBy,
                    createdByUsername = createdByUsername,
                    createdByAvatarUrl = createdByAvatarUrl,
                    serviceLogoUrl = serviceLogoUrl,
                )
            }

        fun createFixedAmount(
            code: String,
            serviceName: String,
            discountAmount: Double,
            category: String = "Unspecified",
            description: String? = null,
            serviceId: ServiceId? = null,
            minimumOrderAmount: Double,
            startDate: Instant,
            endDate: Instant,
            isFirstUserOnly: Boolean = false,
            targetCountries: List<String> = emptyList(),
            createdBy: UserId,
            createdByUsername: String? = null,
            createdByAvatarUrl: String? = null,
            serviceLogoUrl: String? = null
        ): Result<FixedAmountPromoCode> =
            runCatching {
                val cleanCode = code.uppercase().trim()
                val cleanServiceName = serviceName.trim()
                FixedAmountPromoCode(
                    id = PromoCodeId(generateCompositeId(cleanCode, cleanServiceName)),
                    code = cleanCode,
                    serviceName = cleanServiceName,
                    serviceId = serviceId,
                    category = category.trim(),
                    description = description?.trim(),
                    discountAmount = discountAmount,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
                    isFirstUserOnly = isFirstUserOnly,
                    targetCountries = targetCountries.map { it.uppercase() },
                    createdBy = createdBy,
                    createdByUsername = createdByUsername,
                    createdByAvatarUrl = createdByAvatarUrl,
                    serviceLogoUrl = serviceLogoUrl,
                )
            }
    }
}

// ================================================================================================
// COMMENT MODELS
// ================================================================================================

@Serializable
@JvmInline
value class CommentId(val value: String) {
    init {
        require(value.isNotBlank()) { "Comment ID cannot be blank" }
    }

    override fun toString(): String = value
}

@Serializable
enum class CommentParentType {
    PROMO_CODE,
    POST
}

/**
 * Unified comment model for both promo codes and posts.
 * Comments are stored in subcollections: /promocodes/{id}/comments/{commentId} or /posts/{id}/comments/{commentId}
 */
@Serializable
data class Comment(
    val id: CommentId,
    val parentId: String, // PromoCodeId.value or PostId.value
    val parentType: CommentParentType,
    val authorId: UserId,
    val authorUsername: String, // Denormalized from User for fast display
    val authorAvatarUrl: String? = null, // Denormalized from User
    val authorCountry: String? = null, // Denormalized from User for context
    val content: String,
    val imageUrls: List<String> = emptyList(), // Comments can have images
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val createdAt: Instant = Clock.System.now(),
    val isUpvotedByCurrentUser: Boolean = false,
    val isDownvotedByCurrentUser: Boolean = false
) {
    init {
        require(content.isNotBlank()) { "Comment content cannot be blank" }
        require(content.length <= 1000) { "Comment content cannot exceed 1000 characters" }
        require(authorUsername.isNotBlank()) { "Author username cannot be blank" }
        require(upvotes >= 0) { "Upvotes cannot be negative" }
        require(downvotes >= 0) { "Downvotes cannot be negative" }
        require(imageUrls.size <= 3) { "Comment cannot have more than 3 images" }
    }

    val voteScore: Int get() = upvotes - downvotes

    companion object {
        fun create(
            parentId: String,
            parentType: CommentParentType,
            authorId: UserId,
            authorUsername: String,
            content: String,
            authorAvatarUrl: String? = null,
            authorCountry: String? = null,
            imageUrls: List<String> = emptyList()
        ): Result<Comment> =
            runCatching {
                Comment(
                    id = CommentId(generateId()),
                    parentId = parentId,
                    parentType = parentType,
                    authorId = authorId,
                    authorUsername = authorUsername.trim(),
                    authorAvatarUrl = authorAvatarUrl?.trim(),
                    authorCountry = authorCountry?.uppercase()?.trim(),
                    content = content.trim(),
                    imageUrls = imageUrls.map { it.trim() }.filter { it.isNotBlank() },
                )
            }

        private fun generateId(): String = "comment_${Clock.System.now().toEpochMilliseconds()}_${(0..999).random()}"
    }
}

/**
 * Interaction model for comment voting
 */
@Serializable
data class CommentInteraction(
    val commentId: CommentId,
    val userId: UserId,
    val type: CommentInteractionType,
    val createdAt: Instant = Clock.System.now()
)

@Serializable
enum class CommentInteractionType {
    UPVOTE,
    DOWNVOTE,
    REMOVE_VOTE
}

// ================================================================================================
// PROMO MODELS
// ================================================================================================

@Serializable
@JvmInline
value class PromoId(val value: String) {
    init {
        require(value.isNotBlank()) { "Promo ID cannot be blank" }
    }

    override fun toString(): String = value
}

/**
 * Promo model for user-submitted deals without actual promo codes.
 * Different from PromoCode - these are general deals/offers users share.
 */
@Serializable
data class Promo(
    val id: PromoId,
    val title: String,
    val description: String,
    val imageUrls: List<String> = emptyList(),
    val serviceName: String,
    val category: String?,
    val targetCountries: List<String> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val views: Int = 0,
    val shares: Int = 0,
    val isVerified: Boolean = false, // Auto-verified at voteScore >= 10
    val createdBy: UserId,
    val createdAt: Instant = Clock.System.now(),
    val expiresAt: Instant? = null,
    val isUpvotedByCurrentUser: Boolean = false,
    val isDownvotedByCurrentUser: Boolean = false,
    val isBookmarkedByCurrentUser: Boolean = false
) {
    init {
        require(title.isNotBlank()) { "Promo title cannot be blank" }
        require(title.length <= 100) { "Promo title cannot exceed 100 characters" }
        require(description.isNotBlank()) { "Promo description cannot be blank" }
        require(description.length <= 1000) { "Promo description cannot exceed 1000 characters" }
        require(serviceName.isNotBlank()) { "Service name cannot be blank" }
        require(upvotes >= 0) { "Upvotes cannot be negative" }
        require(downvotes >= 0) { "Downvotes cannot be negative" }
        require(views >= 0) { "Views cannot be negative" }
        require(shares >= 0) { "Shares cannot be negative" }
        require(imageUrls.size <= 5) { "Promo cannot have more than 5 images" }
        expiresAt?.let { require(it > createdAt) { "Expiration date must be after creation date" } }
    }

    val voteScore: Int get() = upvotes - downvotes
    val isExpired: Boolean get() = expiresAt?.let { Clock.System.now() > it } ?: false

    companion object {
        fun create(
            description: String,
            serviceName: String,
            createdBy: UserId,
            imageUrls: List<String> = emptyList(),
            category: String = "Unspecified",
            targetCountries: List<String> = emptyList(),
            expiresAt: Instant? = null
        ): Result<Promo> =
            runCatching {
                // Generate title from service name
                val generatedTitle = serviceName.trim().takeIf { it.isNotBlank() }
                    ?: "Promo"

                Promo(
                    id = PromoId(generateId()),
                    title = generatedTitle,
                    description = description.trim(),
                    serviceName = serviceName.trim(),
                    createdBy = createdBy,
                    imageUrls = imageUrls.map { it.trim() }.filter { it.isNotBlank() },
                    category = category.trim(),
                    targetCountries = targetCountries.map { it.uppercase() },
                    expiresAt = expiresAt,
                )
            }

        private fun generateId(): String = "promo_${Clock.System.now().toEpochMilliseconds()}_${(0..999).random()}"
    }
}

// ================================================================================================
// BANNER MODELS
// ================================================================================================

@Serializable
@JvmInline
value class BannerId(val value: String) {
    init {
        require(value.isNotBlank()) { "BannerId cannot be blank" }
    }

    override fun toString(): String = value
}

/**
 * Banner domain model representing promotional banners shown in the home feed.
 * Supports internationalization through country targeting and language arrays.
 */
@Serializable
data class Banner(
    val id: BannerId,
    val imageUrl: String,
    val targetCountries: List<String>, // ISO 3166-1 alpha-2 country codes (ce.g., "KZ", "US", "GB") - empty means global
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
            brandName: String = "Qode"
        ): Banner =
            Banner(
                id = BannerId(id),
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
                createdAt = Clock.System.now().toEpochMilliseconds(),
                updatedAt = Clock.System.now().toEpochMilliseconds(),
                expiresAt = null,
            )
    }
}

// ================================================================================================
// BANNER EXTENSION FUNCTIONS
// ================================================================================================

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
