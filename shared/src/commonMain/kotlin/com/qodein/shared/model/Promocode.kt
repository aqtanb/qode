@file:UseContextualSerialization(Instant::class)

package com.qodein.shared.model

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PromocodeError
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
@JvmInline
value class PromocodeId(val value: String) {
    init {
        require(value.isNotBlank()) { "Post ID cannot be blank" }
        require(value.length == 32) { "Post ID must be 32 characters (UUID without hyphens)" }
        require(value.matches(Regex("^[a-f0-9]+$"))) { "Post ID must be lowercase hex (UUID format)" }
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
    val minimumOrderAmount: Double,
    val startDate: Instant,
    val endDate: Instant,
    val authorId: UserId,
    val serviceName: String,

    val description: String? = null,

    val isFirstUserOnly: Boolean = false,
    val isOneTimeUseOnly: Boolean = false,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val isVerified: Boolean = false,

    val serviceId: ServiceId? = null,
    val serviceLogoUrl: String? = null,

    val authorUsername: String? = null,
    val authorAvatarUrl: String? = null,

    val createdAt: Instant = Clock.System.now(),
) {
    init {
        require(code.isNotBlank()) { "PromoCode code cannot be blank" }
        require(serviceName.isNotBlank()) { "Service name cannot be blank" }
        require(minimumOrderAmount > 0) { "Minimum order amount must be positive" }
        require(upvotes >= 0) { "Upvotes cannot be negative" }
        require(downvotes >= 0) { "Downvotes cannot be negative" }
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
            minimumOrderAmount: Double,
            startDate: Instant,
            endDate: Instant,
            isFirstUserOnly: Boolean,
            isOneTimeUseOnly: Boolean,
            isVerified: Boolean,
            authorId: UserId,
            description: String? = null,
            authorUsername: String? = null,
            authorAvatarUrl: String? = null,
            serviceId: ServiceId? = null,
            serviceLogoUrl: String? = null
        ): Result<PromoCode, PromocodeError.CreationFailure> {
            val cleanCode = code.uppercase().trim()
            val cleanServiceName = serviceName.trim()
            val cleanDescription = description?.trim()

            // Validate inputs
            if (cleanCode.isBlank()) {
                return Result.Error(PromocodeError.CreationFailure.EmptyCode)
            }
            if (cleanServiceName.isBlank()) {
                return Result.Error(PromocodeError.CreationFailure.EmptyServiceName)
            }
            if (minimumOrderAmount <= 0) {
                return Result.Error(PromocodeError.CreationFailure.InvalidMinimumAmount)
            }
            if (endDate <= startDate) {
                return Result.Error(PromocodeError.CreationFailure.InvalidDateRange)
            }

            // Discount validation happens in Discount.Percentage/FixedAmount init blocks
            // No additional validation needed here

            return Result.Success(
                PromoCode(
                    id = PromocodeId(generateCompositeId(cleanCode, cleanServiceName)),
                    code = cleanCode,
                    discount = discount,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
                    authorId = authorId,
                    serviceName = cleanServiceName,
                    description = cleanDescription,
                    isFirstUserOnly = isFirstUserOnly,
                    isOneTimeUseOnly = isOneTimeUseOnly,
                    upvotes = 0,
                    downvotes = 0,
                    isVerified = isVerified,
                    serviceId = serviceId,
                    serviceLogoUrl = serviceLogoUrl?.trim(),
                    authorUsername = authorUsername?.trim(),
                    authorAvatarUrl = authorAvatarUrl?.trim(),
                    createdAt = Clock.System.now()
                )
            )
        }
    }
}
