package com.qodein.shared.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

// ================================================================================================
// POST MODELS
// ================================================================================================

@Serializable
@JvmInline
value class PostId(val value: String) {
    init {
        require(value.isNotBlank()) { "Post ID cannot be blank" }
    }

    override fun toString(): String = value
}

@Serializable
data class Tag(val id: String, val name: String, val color: String? = null) {
    init {
        require(id.isNotBlank()) { "Tag ID cannot be blank" }
        require(name.isNotBlank()) { "Tag name cannot be blank" }
        require(name.length <= 50) { "Tag name cannot exceed 50 characters" }
        color?.let {
            require(it.matches(Regex("^#[0-9A-Fa-f]{6}$"))) { "Color must be a valid hex code" }
        }
    }

    companion object {
        fun create(
            name: String,
            color: String? = null
        ): Tag {
            val cleanName = name.trim().lowercase()
            return Tag(
                id = cleanName.replace(Regex("\\s+"), "_"),
                name = cleanName,
                color = color,
            )
        }
    }
}

@Serializable
data class Post(
    val id: PostId,
    val authorId: UserId,
    val authorUsername: String,
    val authorAvatarUrl: String? = null,
    val authorCountry: String? = null,
    val title: String? = null,
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val tags: List<Tag>,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val shares: Int = 0,
    val createdAt: Instant,
    val isUpvotedByCurrentUser: Boolean = false,
    val isDownvotedByCurrentUser: Boolean = false,
    val isBookmarkedByCurrentUser: Boolean = false
) {
    init {
        require(content.isNotBlank()) { "Post content cannot be blank" }
        require(content.length <= 2000) { "Post content cannot exceed 2000 characters" }
        require(authorUsername.isNotBlank()) { "Author username cannot be blank" }
        title?.let { require(it.length <= 200) { "Post title cannot exceed 200 characters" } }
        require(upvotes >= 0) { "Upvotes cannot be negative" }
        require(downvotes >= 0) { "Downvotes cannot be negative" }
        require(shares >= 0) { "Shares cannot be negative" }
        require(tags.size <= 10) { "Post cannot have more than 10 tags" }
        require(imageUrls.size <= 5) { "Post cannot have more than 5 images" }
    }

    val voteScore: Int get() = upvotes - downvotes

    companion object {
        fun create(
            authorId: UserId,
            authorUsername: String,
            content: String,
            title: String? = null,
            imageUrls: List<String> = emptyList(),
            tags: List<Tag> = emptyList(),
            authorAvatarUrl: String? = null,
            authorCountry: String? = null
        ): Result<Post> =
            runCatching {
                Post(
                    id = PostId(generateId()),
                    authorId = authorId,
                    authorUsername = authorUsername.trim(),
                    authorAvatarUrl = authorAvatarUrl?.trim(),
                    authorCountry = authorCountry?.uppercase()?.trim(),
                    title = title?.trim(),
                    content = content.trim(),
                    imageUrls = imageUrls.map { it.trim() }.filter { it.isNotBlank() },
                    tags = tags,
                    createdAt = Clock.System.now(),
                )
            }

        private fun generateId(): String = "post_${Clock.System.now().toEpochMilliseconds()}_${(0..999).random()}"
    }
}

@Serializable
data class PostInteraction(val postId: PostId, val userId: UserId, val type: InteractionType, val createdAt: Instant = Clock.System.now())

@Serializable
enum class InteractionType {
    UPVOTE,
    DOWNVOTE,
    REMOVE_VOTE,
    COMMENT,
    SHARE,
    BOOKMARK,
    UNBOOKMARK
}

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
    abstract val id: PromoCodeId
    abstract val code: String
    abstract val serviceName: String
    abstract val category: String?
    abstract val title: String
    abstract val description: String?
    abstract val startDate: Instant
    abstract val endDate: Instant
    abstract val isFirstUserOnly: Boolean
    abstract val upvotes: Int
    abstract val downvotes: Int
    abstract val views: Int
    abstract val shares: Int
    abstract val screenshotUrl: String?
    abstract val targetCountries: List<String>
    abstract val isVerified: Boolean
    abstract val createdAt: Instant
    abstract val createdBy: UserId?
    abstract val isUpvotedByCurrentUser: Boolean
    abstract val isDownvotedByCurrentUser: Boolean
    abstract val isBookmarkedByCurrentUser: Boolean

    val isExpired: Boolean get() = Clock.System.now() > endDate
    val isNotStarted: Boolean get() = Clock.System.now() < startDate
    val isValidNow: Boolean get() = !isExpired && !isNotStarted
    val voteScore: Int get() = upvotes - downvotes

    abstract fun calculateDiscount(orderAmount: Double): Double

    @Serializable
    data class PercentagePromoCode(
        override val id: PromoCodeId,
        override val code: String,
        override val serviceName: String,
        override val category: String? = null,
        override val title: String,
        override val description: String? = null,
        val discountPercentage: Double,
        val minimumOrderAmount: Double,
        override val startDate: Instant,
        override val endDate: Instant,
        override val isFirstUserOnly: Boolean = false,
        override val upvotes: Int = 0,
        override val downvotes: Int = 0,
        override val views: Int = 0,
        override val shares: Int = 0,
        override val screenshotUrl: String? = null,
        override val targetCountries: List<String> = emptyList(),
        override val isVerified: Boolean = false,
        override val createdAt: Instant = Clock.System.now(),
        override val createdBy: UserId? = null,
        override val isUpvotedByCurrentUser: Boolean = false,
        override val isDownvotedByCurrentUser: Boolean = false,
        override val isBookmarkedByCurrentUser: Boolean = false
    ) : PromoCode() {
        init {
            require(code.isNotBlank()) { "PromoCode code cannot be blank" }
            require(serviceName.isNotBlank()) { "Service name cannot be blank" }
            require(discountPercentage > 0 && discountPercentage <= 100) { "Discount percentage must be between 0 and 100" }
            require(minimumOrderAmount > 0) { "Minimum order amount must be positive" }
            require(upvotes >= 0) { "Upvotes cannot be negative" }
            require(downvotes >= 0) { "Downvotes cannot be negative" }
            require(views >= 0) { "Views cannot be negative" }
            require(shares >= 0) { "Shares cannot be negative" }
            require(endDate > startDate) { "End date must be after start date" }
        }

        override fun calculateDiscount(orderAmount: Double): Double = discountPercentage
    }

    @Serializable
    data class FixedAmountPromoCode(
        override val id: PromoCodeId,
        override val code: String,
        override val serviceName: String,
        override val category: String? = null,
        override val title: String,
        override val description: String? = null,
        val discountAmount: Double,
        val minimumOrderAmount: Double,
        override val startDate: Instant,
        override val endDate: Instant,
        override val isFirstUserOnly: Boolean = false,
        override val upvotes: Int = 0,
        override val downvotes: Int = 0,
        override val views: Int = 0,
        override val shares: Int = 0,
        override val screenshotUrl: String? = null,
        override val targetCountries: List<String> = emptyList(),
        override val isVerified: Boolean = false,
        override val createdAt: Instant = Clock.System.now(),
        override val createdBy: UserId? = null,
        override val isUpvotedByCurrentUser: Boolean = false,
        override val isDownvotedByCurrentUser: Boolean = false,
        override val isBookmarkedByCurrentUser: Boolean = false
    ) : PromoCode() {
        init {
            require(code.isNotBlank()) { "PromoCode code cannot be blank" }
            require(serviceName.isNotBlank()) { "Service name cannot be blank" }
            require(discountAmount > 0) { "Discount amount must be positive" }
            require(minimumOrderAmount > 0) { "Minimum order amount must be positive" }
            require(upvotes >= 0) { "Upvotes cannot be negative" }
            require(downvotes >= 0) { "Downvotes cannot be negative" }
            require(views >= 0) { "Views cannot be negative" }
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
            val cleanCode = code.uppercase().trim().replace(" ", "_")
            val cleanService = serviceName.uppercase().trim().replace(" ", "_")
            return "${cleanCode}_$cleanService"
        }

        fun createPercentage(
            code: String,
            serviceName: String,
            discountPercentage: Double,
            category: String? = null,
            title: String,
            description: String? = null,
            minimumOrderAmount: Double,
            startDate: Instant,
            endDate: Instant,
            isFirstUserOnly: Boolean = false,
            screenshotUrl: String? = null,
            targetCountries: List<String> = emptyList(),
            createdBy: UserId? = null
        ): Result<PercentagePromoCode> =
            runCatching {
                val cleanCode = code.uppercase().trim()
                val cleanServiceName = serviceName.trim()
                PercentagePromoCode(
                    id = PromoCodeId(generateCompositeId(cleanCode, cleanServiceName)),
                    code = cleanCode,
                    serviceName = cleanServiceName,
                    category = category?.trim(),
                    title = title.trim(),
                    description = description?.trim(),
                    discountPercentage = discountPercentage,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
                    isFirstUserOnly = isFirstUserOnly,
                    screenshotUrl = screenshotUrl?.trim(),
                    targetCountries = targetCountries.map { it.uppercase() },
                    createdBy = createdBy,
                )
            }

        fun createFixedAmount(
            code: String,
            serviceName: String,
            discountAmount: Double,
            category: String? = null,
            title: String,
            description: String? = null,
            minimumOrderAmount: Double,
            startDate: Instant,
            endDate: Instant,
            isFirstUserOnly: Boolean = false,
            screenshotUrl: String? = null,
            targetCountries: List<String> = emptyList(),
            createdBy: UserId? = null
        ): Result<FixedAmountPromoCode> =
            runCatching {
                val cleanCode = code.uppercase().trim()
                val cleanServiceName = serviceName.trim()
                FixedAmountPromoCode(
                    id = PromoCodeId(generateCompositeId(cleanCode, cleanServiceName)),
                    code = cleanCode,
                    serviceName = cleanServiceName,
                    category = category?.trim(),
                    title = title.trim(),
                    description = description?.trim(),
                    discountAmount = discountAmount,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
                    isFirstUserOnly = isFirstUserOnly,
                    screenshotUrl = screenshotUrl?.trim(),
                    targetCountries = targetCountries.map { it.uppercase() },
                    createdBy = createdBy,
                )
            }
    }
}

@Serializable
data class PromoCodeVote(
    val id: String,
    val promoCodeId: PromoCodeId,
    val userId: UserId,
    val isUpvote: Boolean,
    val votedAt: Instant = Clock.System.now()
) {
    companion object {
        fun create(
            promoCodeId: PromoCodeId,
            userId: UserId,
            isUpvote: Boolean
        ): PromoCodeVote =
            PromoCodeVote(
                id = generateId(promoCodeId.value, userId.value, isUpvote),
                promoCodeId = promoCodeId,
                userId = userId,
                isUpvote = isUpvote,
            )

        private fun generateId(
            promoCodeId: String,
            userId: String,
            isUpvote: Boolean
        ): String = "${promoCodeId}_${userId}_${if (isUpvote) "up" else "down"}"
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
            title: String,
            description: String,
            serviceName: String,
            createdBy: UserId,
            imageUrls: List<String> = emptyList(),
            category: String? = null,
            targetCountries: List<String> = emptyList(),
            expiresAt: Instant? = null
        ): Result<Promo> =
            runCatching {
                Promo(
                    id = PromoId(generateId()),
                    title = title.trim(),
                    description = description.trim(),
                    serviceName = serviceName.trim(),
                    createdBy = createdBy,
                    imageUrls = imageUrls.map { it.trim() }.filter { it.isNotBlank() },
                    category = category?.trim(),
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
