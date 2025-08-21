package com.qodein.shared.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

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
    abstract val title: String
    abstract val description: String?
    abstract val startDate: Instant
    abstract val endDate: Instant
    abstract val isFirstUserOnly: Boolean
    abstract val upvotes: Int
    abstract val downvotes: Int
    abstract val views: Int
    abstract val screenshotUrl: String?
    abstract val comments: List<String>?
    abstract val createdAt: Instant
    abstract val createdBy: UserId?

    val isExpired: Boolean get() = Clock.System.now() > endDate
    val isNotStarted: Boolean get() = Clock.System.now() < startDate
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
        override val screenshotUrl: String? = null,
        override val comments: List<String>? = null,
        override val createdAt: Instant = Clock.System.now(),
        override val createdBy: UserId? = null
    ) : PromoCode() {
        init {
            require(code.isNotBlank()) { "PromoCode code cannot be blank" }
            require(serviceName.isNotBlank()) { "Service name cannot be blank" }
            require(discountPercentage > 0 && discountPercentage <= 100) { "Discount percentage must be between 0 and 100" }
            require(minimumOrderAmount?.let { it > 0 } ?: true) { "Minimum order amount must be positive" }
            require(upvotes >= 0) { "Upvotes cannot be negative" }
            require(downvotes >= 0) { "Downvotes cannot be negative" }
            require(views >= 0) { "Views cannot be negative" }
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
        override val screenshotUrl: String? = null,
        override val comments: List<String>? = null,
        override val createdAt: Instant = Clock.System.now(),
        override val createdBy: UserId? = null
    ) : PromoCode() {
        init {
            require(code.isNotBlank()) { "PromoCode code cannot be blank" }
            require(serviceName.isNotBlank()) { "Service name cannot be blank" }
            require(discountAmount > 0) { "Discount amount must be positive" }
            require(minimumOrderAmount?.let { it > 0 } ?: true) { "Minimum order amount must be positive" }
            require(upvotes >= 0) { "Upvotes cannot be negative" }
            require(downvotes >= 0) { "Downvotes cannot be negative" }
            require(views >= 0) { "Views cannot be negative" }
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
                    title = title.trim(),
                    description = description?.trim(),
                    discountPercentage = discountPercentage,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
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
            title: String,
            description: String? = null,
            minimumOrderAmount: Double,
            startDate: Instant,
            endDate: Instant,
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
                    title = title.trim(),
                    description = description?.trim(),
                    discountAmount = discountAmount,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
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
