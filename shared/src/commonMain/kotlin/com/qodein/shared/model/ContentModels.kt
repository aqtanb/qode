@file:UseContextualSerialization(Instant::class) // This line is conceptual, you typically add @Contextual per property

package com.qodein.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant

// MARK: - Promocode

@Serializable
@JvmInline
value class PromocodeId(val value: String) {
    init {
        require(value.isNotBlank()) { "PromoCode ID cannot be blank" }
    }

    override fun toString(): String = value
}

/**
 * Discount type for promocodes.
 * Sealed interface ensures type safety while avoiding boilerplate.
 */
@Serializable
sealed interface Discount {
    val value: Double

    @Serializable
    data class Percentage(override val value: Double) : Discount {
        init {
            require(value > 0 && value <= 100) { "Percentage must be between 0 and 100" }
        }
    }

    @Serializable
    data class FixedAmount(override val value: Double) : Discount {
        init {
            require(value > 0) { "Fixed amount must be positive" }
        }
    }
}

@Serializable
data class PromoCode(
    val id: PromocodeId,
    val code: String,
    val discount: Discount,
    val serviceId: ServiceId? = null,
    val serviceName: String,
    val category: String = "Unspecified",
    val description: String? = null,
    val minimumOrderAmount: Double,
    val startDate: Instant,
    val endDate: Instant,
    val isFirstUserOnly: Boolean = false,
    val isOneTimeUseOnly: Boolean = false,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val shares: Int = 0,
    val targetCountries: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val createdAt: Instant = Clock.System.now(),
    val createdBy: UserId,
    val createdByUsername: String? = null,
    val createdByAvatarUrl: String? = null,
    val serviceLogoUrl: String? = null
) {
    init {
        require(code.isNotBlank()) { "PromoCode code cannot be blank" }
        require(serviceName.isNotBlank()) { "Service name cannot be blank" }
        require(minimumOrderAmount > 0) { "Minimum order amount must be positive" }
        require(upvotes >= 0) { "Upvotes cannot be negative" }
        require(downvotes >= 0) { "Downvotes cannot be negative" }
        require(shares >= 0) { "Shares cannot be negative" }
        require(endDate > startDate) { "End date must be after start date" }
    }

    val isExpired: Boolean get() = Clock.System.now() > endDate
    val isNotStarted: Boolean get() = Clock.System.now() < startDate
    val isValidNow: Boolean get() = !isExpired && !isNotStarted
    val voteScore: Int get() = upvotes - downvotes

    fun calculateDiscount(orderAmount: Double): Double =
        when (discount) {
            is Discount.Percentage -> discount.value
            is Discount.FixedAmount -> discount.value * 100 / orderAmount
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

        fun create(
            code: String,
            serviceName: String,
            discount: Discount,
            serviceId: ServiceId? = null,
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
        ): Result<PromoCode> =
            runCatching {
                val cleanCode = code.uppercase().trim()
                val cleanServiceName = serviceName.trim()
                PromoCode(
                    id = PromocodeId(generateCompositeId(cleanCode, cleanServiceName)),
                    code = cleanCode,
                    discount = discount,
                    serviceName = cleanServiceName,
                    serviceId = serviceId,
                    category = category.trim(),
                    description = description?.trim(),
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

// MARK: - Banner
