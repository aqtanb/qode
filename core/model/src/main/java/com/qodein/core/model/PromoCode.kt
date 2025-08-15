package com.qodein.core.model

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID.randomUUID

@Serializable
data class PromoCodeId(val value: String) {
    init {
        require(value.isNotBlank()) { "PromoCode ID cannot be blank" }
    }
}

@Serializable
sealed class PromoCode {
    abstract val id: PromoCodeId
    abstract val code: String
    abstract val serviceName: String
    abstract val category: String?
    abstract val title: String?
    abstract val description: String?
    abstract val startDate: Instant?
    abstract val endDate: Instant?
    abstract val usageLimit: Int?
    abstract val isFirstUserOnly: Boolean
    abstract val upvotes: Int
    abstract val downvotes: Int
    abstract val views: Int
    abstract val screenshotUrl: String?
    abstract val comments: List<String>?
    abstract val createdAt: Instant
    abstract val updatedAt: Instant
    abstract val createdBy: UserId?

    val isExpired: Boolean get() = endDate?.let { Instant.now().isAfter(it) } ?: false
    val isNotStarted: Boolean get() = startDate?.let { Instant.now().isBefore(it) } ?: false
    val isValidNow: Boolean get() = !isExpired && !isNotStarted
    val totalVotes: Int get() = upvotes + downvotes
    val voteScore: Int get() = upvotes - downvotes
    val popularityScore: Double get() = if (totalVotes > 0) upvotes.toDouble() / totalVotes else 0.5

    abstract fun calculateDiscount(orderAmount: Double): Double

    @Serializable
    data class PercentagePromoCode(
        override val id: PromoCodeId,
        override val code: String,
        override val serviceName: String,
        override val category: String? = null,
        override val title: String? = null,
        override val description: String? = null,
        val discountPercentage: Double,
        val minimumOrderAmount: Double? = null,
        val maximumDiscount: Double,
        override val startDate: Instant? = null,
        override val endDate: Instant? = null,
        override val usageLimit: Int? = null,
        override val isFirstUserOnly: Boolean = false,
        override val upvotes: Int = 0,
        override val downvotes: Int = 0,
        override val views: Int = 0,
        override val screenshotUrl: String? = null,
        override val comments: List<String>? = null,
        override val createdAt: Instant = Instant.now(),
        override val updatedAt: Instant = Instant.now(),
        override val createdBy: UserId? = null
    ) : PromoCode() {
        init {
            require(code.isNotBlank()) { "PromoCode code cannot be blank" }
            require(serviceName.isNotBlank()) { "Service name cannot be blank" }
            require(discountPercentage > 0 && discountPercentage <= 100) { "Discount percentage must be between 0 and 100" }
            require(maximumDiscount > 0) { "Maximum discount must be positive" }
            require(minimumOrderAmount?.let { it > 0 } ?: true) { "Minimum order amount must be positive" }
            require(usageLimit?.let { it > 0 } ?: true) { "Usage limit must be positive" }
            require(upvotes >= 0) { "Upvotes cannot be negative" }
            require(downvotes >= 0) { "Downvotes cannot be negative" }
            require(views >= 0) { "Views cannot be negative" }
            endDate?.let { end ->
                startDate?.let { start ->
                    require(end.isAfter(start)) { "End date must be after start date" }
                }
            }
        }

        override fun calculateDiscount(orderAmount: Double): Double {
            if (!isValidNow || orderAmount < (minimumOrderAmount ?: 0.0)) {
                return 0.0
            }
            val discount = (orderAmount * discountPercentage) / 100.0
            return minOf(discount, maximumDiscount)
        }
    }

    @Serializable
    data class FixedAmountPromoCode(
        override val id: PromoCodeId,
        override val code: String,
        override val serviceName: String,
        override val category: String? = null,
        override val title: String? = null,
        override val description: String? = null,
        val discountAmount: Double,
        val minimumOrderAmount: Double? = null,
        override val startDate: Instant? = null,
        override val endDate: Instant? = null,
        override val usageLimit: Int? = null,
        override val isFirstUserOnly: Boolean = false,
        override val upvotes: Int = 0,
        override val downvotes: Int = 0,
        override val views: Int = 0,
        override val screenshotUrl: String? = null,
        override val comments: List<String>? = null,
        override val createdAt: Instant = Instant.now(),
        override val updatedAt: Instant = Instant.now(),
        override val createdBy: UserId? = null
    ) : PromoCode() {
        init {
            require(code.isNotBlank()) { "PromoCode code cannot be blank" }
            require(serviceName.isNotBlank()) { "Service name cannot be blank" }
            require(discountAmount > 0) { "Discount amount must be positive" }
            require(minimumOrderAmount?.let { it > 0 } ?: true) { "Minimum order amount must be positive" }
            require(usageLimit?.let { it > 0 } ?: true) { "Usage limit must be positive" }
            require(upvotes >= 0) { "Upvotes cannot be negative" }
            require(downvotes >= 0) { "Downvotes cannot be negative" }
            require(views >= 0) { "Views cannot be negative" }
            endDate?.let { end ->
                startDate?.let { start ->
                    require(end.isAfter(start)) { "End date must be after start date" }
                }
            }
        }

        override fun calculateDiscount(orderAmount: Double): Double {
            if (!isValidNow || orderAmount < (minimumOrderAmount ?: 0.0)) {
                return 0.0
            }
            return discountAmount
        }
    }

    @Serializable
    data class PromoPromoCode(
        override val id: PromoCodeId,
        override val code: String,
        override val serviceName: String,
        override val category: String? = null,
        override val title: String? = null,
        override val description: String,
        override val startDate: Instant? = null,
        override val endDate: Instant? = null,
        override val usageLimit: Int? = null,
        override val isFirstUserOnly: Boolean = false,
        override val upvotes: Int = 0,
        override val downvotes: Int = 0,
        override val views: Int = 0,
        override val screenshotUrl: String? = null,
        override val comments: List<String>? = null,
        override val createdAt: Instant = Instant.now(),
        override val updatedAt: Instant = Instant.now(),
        override val createdBy: UserId? = null
    ) : PromoCode() {
        init {
            require(code.isNotBlank()) { "PromoCode code cannot be blank" }
            require(serviceName.isNotBlank()) { "Service name cannot be blank" }
            require(description.isNotBlank()) { "Promo description cannot be blank" }
            require(usageLimit?.let { it > 0 } ?: true) { "Usage limit must be positive" }
            require(upvotes >= 0) { "Upvotes cannot be negative" }
            require(downvotes >= 0) { "Downvotes cannot be negative" }
            require(views >= 0) { "Views cannot be negative" }
            endDate?.let { end ->
                startDate?.let { start ->
                    require(end.isAfter(start)) { "End date must be after start date" }
                }
            }
        }

        override fun calculateDiscount(orderAmount: Double): Double = 0.0 // Promos don't calculate discounts
    }

    companion object {
        private fun generateId(): String = randomUUID().toString()

        /**
         * Generate composite ID from code and service name.
         * Format: "CODE_SERVICENAME" (both uppercase, spaces replaced with underscores)
         */
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
            maximumDiscount: Double,
            category: String? = null,
            title: String? = null,
            description: String? = null,
            minimumOrderAmount: Double? = null,
            startDate: Instant? = null,
            endDate: Instant? = null,
            usageLimit: Int? = null,
            isFirstUserOnly: Boolean = false,
            screenshotUrl: String? = null,
            comments: List<String>? = null,
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
                    title = title?.trim(),
                    description = description?.trim(),
                    discountPercentage = discountPercentage,
                    maximumDiscount = maximumDiscount,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
                    usageLimit = usageLimit,
                    isFirstUserOnly = isFirstUserOnly,
                    screenshotUrl = screenshotUrl?.trim(),
                    comments = comments,
                    createdBy = createdBy,
                )
            }

        fun createFixedAmount(
            code: String,
            serviceName: String,
            discountAmount: Double,
            category: String? = null,
            title: String? = null,
            description: String? = null,
            minimumOrderAmount: Double? = null,
            startDate: Instant? = null,
            endDate: Instant? = null,
            usageLimit: Int? = null,
            isFirstUserOnly: Boolean = false,
            screenshotUrl: String? = null,
            comments: List<String>? = null,
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
                    title = title?.trim(),
                    description = description?.trim(),
                    discountAmount = discountAmount,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
                    usageLimit = usageLimit,
                    isFirstUserOnly = isFirstUserOnly,
                    screenshotUrl = screenshotUrl?.trim(),
                    comments = comments,
                    createdBy = createdBy,
                )
            }

        fun createPromo(
            code: String,
            serviceName: String,
            description: String,
            category: String? = null,
            title: String? = null,
            startDate: Instant? = null,
            endDate: Instant? = null,
            usageLimit: Int? = null,
            isFirstUserOnly: Boolean = false,
            screenshotUrl: String? = null,
            comments: List<String>? = null,
            createdBy: UserId? = null
        ): Result<PromoPromoCode> =
            runCatching {
                val cleanCode = code.uppercase().trim()
                val cleanServiceName = serviceName.trim()
                PromoPromoCode(
                    id = PromoCodeId(generateCompositeId(cleanCode, cleanServiceName)),
                    code = cleanCode,
                    serviceName = cleanServiceName,
                    category = category?.trim(),
                    title = title?.trim(),
                    description = description.trim(),
                    startDate = startDate,
                    endDate = endDate,
                    usageLimit = usageLimit,
                    isFirstUserOnly = isFirstUserOnly,
                    screenshotUrl = screenshotUrl?.trim(),
                    comments = comments,
                    createdBy = createdBy,
                )
            }
    }
}

@Serializable
data class PromoCodeVote(
    val id: String = randomUUID().toString(),
    val promoCodeId: PromoCodeId,
    val userId: UserId,
    val isUpvote: Boolean,
    val votedAt: Instant = Instant.now()
)

@Serializable
data class PromoCodeUsage(
    val id: String = randomUUID().toString(),
    val promoCodeId: PromoCodeId,
    val userId: UserId,
    val orderAmount: Double,
    val discountAmount: Double,
    val usedAt: Instant = Instant.now()
) {
    init {
        require(orderAmount > 0) { "Order amount must be positive" }
        require(discountAmount >= 0) { "Discount amount cannot be negative" }
    }
}
