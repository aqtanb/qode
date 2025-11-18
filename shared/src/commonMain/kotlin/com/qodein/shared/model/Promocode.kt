@file:UseContextualSerialization(Instant::class)

package com.qodein.shared.model

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PromocodeError
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Promocode ID - composite key format: {serviceName}_{code}
 * Validation is done in PromoCode.create() for rich error handling.
 */
@Serializable
@JvmInline
value class PromocodeId(val value: String) {
    override fun toString(): String = value
}

/**
 * Discount type for promocodes.
 * Sealed interface ensures type safety while avoiding boilerplate.
 * Validation is done in PromoCode.create() for rich error handling.
 */
@Serializable
sealed interface Discount {
    val value: Double

    @Serializable
    data class Percentage(override val value: Double) : Discount

    @Serializable
    data class FixedAmount(override val value: Double) : Discount
}

@ConsistentCopyVisibility
@Serializable
data class PromoCode private constructor(
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

    val createdAt: Instant = Clock.System.now()
) {
    val voteScore: Int get() = upvotes - downvotes

    companion object {
        const val CODE_MIN_LENGTH = 2
        const val CODE_MAX_LENGTH = 50
        const val DESCRIPTION_MAX_LENGTH = 1000

        fun create(
            code: String,
            service: Service,
            author: User,
            discount: Discount,
            minimumOrderAmount: Double,
            startDate: Instant,
            endDate: Instant,
            isFirstUserOnly: Boolean,
            isOneTimeUseOnly: Boolean,
            isVerified: Boolean,
            description: String? = null
        ): Result<PromoCode, PromocodeError.CreationFailure> {
            val cleanCode = code.uppercase().trim()
            val cleanDescription = description?.trim()

            if (cleanCode.isBlank()) {
                return Result.Error(PromocodeError.CreationFailure.EmptyCode)
            }
            if (cleanCode.length < CODE_MIN_LENGTH) {
                return Result.Error(PromocodeError.CreationFailure.CodeTooShort)
            }
            if (cleanCode.length > CODE_MAX_LENGTH) {
                return Result.Error(PromocodeError.CreationFailure.CodeTooLong)
            }

            if (minimumOrderAmount <= 0) {
                return Result.Error(PromocodeError.CreationFailure.InvalidMinimumAmount)
            }

            when (discount) {
                is Discount.Percentage -> {
                    if (discount.value <= 0 || discount.value > 100) {
                        return Result.Error(PromocodeError.CreationFailure.InvalidPercentageDiscount)
                    }
                }
                is Discount.FixedAmount -> {
                    if (discount.value <= 0) {
                        return Result.Error(PromocodeError.CreationFailure.InvalidFixedAmountDiscount)
                    }
                    if (discount.value > minimumOrderAmount) {
                        return Result.Error(PromocodeError.CreationFailure.DiscountExceedsMinimumAmount)
                    }
                }
            }

            cleanDescription?.let {
                if (it.length > DESCRIPTION_MAX_LENGTH) {
                    return Result.Error(PromocodeError.CreationFailure.DescriptionTooLong)
                }
            }

            if (endDate <= startDate) {
                return Result.Error(PromocodeError.CreationFailure.InvalidDateRange)
            }

            val compositeId = generateCompositeId(cleanCode, service.name)
            if (compositeId.isBlank() || compositeId.length > 200) {
                return Result.Error(PromocodeError.CreationFailure.InvalidPromocodeId)
            }

            return Result.Success(
                PromoCode(
                    id = PromocodeId(compositeId),
                    code = cleanCode,
                    discount = discount,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
                    authorId = author.id,
                    serviceName = service.name,
                    description = cleanDescription,
                    isFirstUserOnly = isFirstUserOnly,
                    isOneTimeUseOnly = isOneTimeUseOnly,
                    upvotes = 0,
                    downvotes = 0,
                    isVerified = isVerified,
                    serviceId = service.id,
                    serviceLogoUrl = service.logoUrl,
                    authorUsername = author.displayName,
                    authorAvatarUrl = author.profile.photoUrl,
                    createdAt = Clock.System.now(),
                ),
            )
        }

        /**
         * Reconstruct a PromoCode from storage/DTO (for mappers/repositories only).
         * Assumes data is already validated. No sanitization performed.
         */
        fun fromDto(
            id: PromocodeId,
            code: String,
            discount: Discount,
            minimumOrderAmount: Double,
            startDate: Instant,
            endDate: Instant,
            authorId: UserId,
            serviceName: String,
            description: String? = null,
            isFirstUserOnly: Boolean = false,
            isOneTimeUseOnly: Boolean = false,
            upvotes: Int = 0,
            downvotes: Int = 0,
            isVerified: Boolean = false,
            serviceId: ServiceId? = null,
            serviceLogoUrl: String? = null,
            authorUsername: String? = null,
            authorAvatarUrl: String? = null,
            createdAt: Instant
        ): PromoCode =
            PromoCode(
                id = id,
                code = code,
                discount = discount,
                minimumOrderAmount = minimumOrderAmount,
                startDate = startDate,
                endDate = endDate,
                authorId = authorId,
                serviceName = serviceName,
                description = description,
                isFirstUserOnly = isFirstUserOnly,
                isOneTimeUseOnly = isOneTimeUseOnly,
                upvotes = upvotes,
                downvotes = downvotes,
                isVerified = isVerified,
                serviceId = serviceId,
                serviceLogoUrl = serviceLogoUrl,
                authorUsername = authorUsername,
                authorAvatarUrl = authorAvatarUrl,
                createdAt = createdAt,
            )
    }
}

private fun generateCompositeId(
    code: String,
    serviceName: String
): String {
    val cleanCode = code.lowercase().trim().replace(Regex("\\s+"), "_")
    val cleanService = serviceName.lowercase().trim().replace(Regex("\\s+"), "_")
    return "${cleanService}_$cleanCode"
}
