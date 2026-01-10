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
 * Validation is handled in PromocodeId.create() to keep rules centralized.
 */
@Serializable
@JvmInline
value class PromocodeId(val value: String) {
    override fun toString(): String = value

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
        private const val MAX_ID_LENGTH = 200

        /**
         * Creates a composite ID from service name and code.
         * Normalizes inputs by lowercasing and replacing spaces with underscores.
         */
        fun create(
            serviceName: String,
            code: String
        ): Result<PromocodeId, PromocodeError.CreationFailure> {
            val cleanCode = code.lowercase().trim().replace(WHITESPACE_REGEX, "_")
            val cleanService = serviceName.lowercase().trim().replace(WHITESPACE_REGEX, "_")
            val compositeId = "${cleanService}_$cleanCode"

            if (compositeId.isBlank() || compositeId.length > MAX_ID_LENGTH) {
                return Result.Error(PromocodeError.CreationFailure.InvalidPromocodeId)
            }

            return Result.Success(PromocodeId(compositeId))
        }
    }
}

@Serializable
@JvmInline
value class PromocodeCode(val value: String) {
    override fun toString(): String = value

    companion object {
        const val MIN_LENGTH = 1
        const val MAX_LENGTH = 50

        fun create(rawCode: String): Result<PromocodeCode, PromocodeError.CreationFailure> {
            val normalized = rawCode.trim()

            if (normalized.isEmpty()) {
                return Result.Error(PromocodeError.CreationFailure.EmptyCode)
            }
            if (normalized.length < MIN_LENGTH) {
                return Result.Error(PromocodeError.CreationFailure.CodeTooShort)
            }
            if (normalized.length > MAX_LENGTH) {
                return Result.Error(PromocodeError.CreationFailure.CodeTooLong)
            }

            return Result.Success(PromocodeCode(normalized))
        }

        /**
         * Creates a code from already validated/persisted data (e.g., Firestore DTOs).
         * Bypasses validation since data is assumed to be trusted.
         */
        fun fromRaw(rawCode: String): PromocodeCode = PromocodeCode(rawCode)
    }
}

/**
 * Discount type for promocodes.
 * Sealed interface ensures type safety while avoiding boilerplate.
 * Validation is done in Promocode.create() for rich error handling.
 */
@Serializable
sealed interface Discount {
    val value: Double

    fun validate(minimumOrderAmount: Double): Result<Unit, PromocodeError.CreationFailure> =
        when (this) {
            is Percentage -> {
                if (value <= 0 || value > 100) {
                    Result.Error(PromocodeError.CreationFailure.InvalidPercentageDiscount)
                } else {
                    Result.Success(Unit)
                }
            }
            is FixedAmount -> {
                if (value <= 0) {
                    Result.Error(PromocodeError.CreationFailure.InvalidFixedAmountDiscount)
                } else if (value > minimumOrderAmount) {
                    Result.Error(PromocodeError.CreationFailure.DiscountExceedsMinimumAmount)
                } else {
                    Result.Success(Unit)
                }
            }
            is FreeItem -> {
                val trimmed = description.trim()
                when {
                    trimmed.isBlank() -> Result.Error(PromocodeError.CreationFailure.InvalidFreeItemDescription)
                    trimmed.length > FreeItem.MAX_DESCRIPTION_LENGTH -> Result.Error(
                        PromocodeError.CreationFailure.FreeItemDescriptionTooLong,
                    )
                    trimmed.any { it.isISOControl() } -> Result.Error(PromocodeError.CreationFailure.InvalidFreeItemDescription)
                    trimmed.any { !it.isLetterOrDigit() && it != ' ' && it != '-' } -> Result.Error(
                        PromocodeError.CreationFailure.FreeItemDescriptionInvalidCharacters,
                    )
                    else -> Result.Success(Unit)
                }
            }
        }

    @Serializable
    data class Percentage(override val value: Double) : Discount

    @Serializable
    data class FixedAmount(override val value: Double) : Discount

    @Serializable
    data class FreeItem(val description: String) : Discount {
        override val value: Double get() = 0.0

        companion object {
            const val MAX_DESCRIPTION_LENGTH = 50
        }
    }
}

@ConsistentCopyVisibility
@Serializable
data class Promocode private constructor(
    val id: PromocodeId,
    val code: PromocodeCode,
    val discount: Discount,
    val minimumOrderAmount: Double,
    val startDate: Instant,
    val endDate: Instant,
    val authorId: UserId,
    val serviceName: String,
    val description: String? = null,

    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val isVerified: Boolean = false,

    val serviceId: ServiceId? = null,
    val serviceLogoUrl: String? = null,
    val serviceSiteUrl: String? = null,

    val authorUsername: String? = null,
    val authorAvatarUrl: String? = null,

    val createdAt: Instant = Clock.System.now()
) {
    val voteScore: Int get() = upvotes - downvotes

    companion object {
        const val DESCRIPTION_MAX_LENGTH = 1000
        const val MINIMUM_ORDER_AMOUNT_MAX_LENGTH = 50

        fun create(
            code: String,
            service: Service,
            author: User,
            discount: Discount,
            minimumOrderAmount: Double,
            startDate: Instant,
            endDate: Instant,
            isVerified: Boolean,
            description: String? = null
        ): Result<Promocode, PromocodeError.CreationFailure> {
            val promoCode = when (val result = PromocodeCode.create(code)) {
                is Result.Error -> return Result.Error(result.error)
                is Result.Success -> result.data
            }
            val cleanDescription = description?.trim()

            if (minimumOrderAmount <= 0) {
                return Result.Error(PromocodeError.CreationFailure.InvalidMinimumAmount)
            }

            when (val validation = discount.validate(minimumOrderAmount)) {
                is Result.Error -> return Result.Error(validation.error)
                is Result.Success -> Unit
            }

            cleanDescription?.let {
                if (it.length > DESCRIPTION_MAX_LENGTH) {
                    return Result.Error(PromocodeError.CreationFailure.DescriptionTooLong)
                }
            }

            if (endDate <= startDate) {
                return Result.Error(PromocodeError.CreationFailure.InvalidDateRange)
            }

            val promoId = when (
                val idResult = PromocodeId.create(
                    serviceName = service.name,
                    code = promoCode.value,
                )
            ) {
                is Result.Error -> return Result.Error(idResult.error)
                is Result.Success -> idResult.data
            }

            return Result.Success(
                Promocode(
                    id = promoId,
                    code = promoCode,
                    discount = discount,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
                    authorId = author.id,
                    serviceName = service.name,
                    description = cleanDescription,
                    upvotes = 0,
                    downvotes = 0,
                    isVerified = isVerified,
                    serviceId = service.id,
                    serviceLogoUrl = service.logoUrl,
                    serviceSiteUrl = service.siteUrl,
                    authorUsername = author.displayName,
                    authorAvatarUrl = author.profile.photoUrl,
                    createdAt = Clock.System.now(),
                ),
            )
        }

        /**
         * Reconstruct a Promocode from storage/DTO (for mappers/repositories only).
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
            upvotes: Int = 0,
            downvotes: Int = 0,
            isVerified: Boolean = false,
            serviceId: ServiceId? = null,
            serviceLogoUrl: String? = null,
            serviceSiteUrl: String? = null,
            authorUsername: String? = null,
            authorAvatarUrl: String? = null,
            createdAt: Instant
        ): Promocode =
            Promocode(
                id = id,
                code = PromocodeCode.fromRaw(code),
                discount = discount,
                minimumOrderAmount = minimumOrderAmount,
                startDate = startDate,
                endDate = endDate,
                authorId = authorId,
                serviceName = serviceName,
                description = description,
                upvotes = upvotes,
                downvotes = downvotes,
                isVerified = isVerified,
                serviceId = serviceId,
                serviceLogoUrl = serviceLogoUrl,
                serviceSiteUrl = serviceSiteUrl,
                authorUsername = authorUsername,
                authorAvatarUrl = authorAvatarUrl,
                createdAt = createdAt,
            )
    }
}
