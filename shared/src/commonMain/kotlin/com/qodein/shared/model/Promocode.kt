@file:OptIn(ExperimentalTime::class)

package com.qodein.shared.model

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PromocodeError
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
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
                if (value !in 0.0..100.0) {
                    Result.Error(PromocodeError.CreationFailure.InvalidPercentageDiscount)
                } else {
                    Result.Success(Unit)
                }
            }
            is FixedAmount -> {
                if (value < 0) {
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
data class Promocode
@OptIn(ExperimentalTime::class)
private constructor(
    val id: PromocodeId,
    val code: String,
    val discount: Discount,
    val minimumOrderAmount: Double,
    val startDate: Instant,
    val endDate: Instant,
    val serviceName: String,
    val description: String? = null,

    val upvotes: Int,
    val downvotes: Int,
    val voteScore: Int,

    val isVerified: Boolean = false,

    val serviceId: ServiceId?,
    val serviceLogoUrl: String?,
    val serviceSiteUrl: String?,

    val authorId: UserId,
    val authorUsername: String?,
    val authorAvatarUrl: String?,

    val imageUrls: List<String> = emptyList(),

    val createdAt: Instant
) {

    companion object {
        const val CODE_MAX_LENGTH = 50
        const val DESCRIPTION_MAX_LENGTH = 1000
        const val MINIMUM_ORDER_AMOUNT_MAX_LENGTH = 50
        const val DISCOUNT_AMOUNT_MAX_LENGTH = 50
        const val DISCOUNT_PERCENTAGE_MAX_LENGTH = 50
        const val MINIMUM_MONETARY_VALUE = 0.0
        const val MAX_DECIMAL_PLACES = 2
        const val PERCENTAGE_MIN_VALUE = 1.0
        const val PERCENTAGE_MAX_VALUE = 100.0
        const val DEFAULT_PAGE_SIZE = 5
        const val MAX_IMAGES = 3

        fun sanitizeCode(code: String): String = code.trim()

        fun sanitizeMonetaryValue(value: String): String = value.trim()

        fun sanitizeDescription(description: String): String = description.trim()

        fun validateCode(code: String): Result<Unit, PromocodeError.CreationFailure> =
            when {
                code.isEmpty() -> Result.Error(PromocodeError.CreationFailure.EmptyCode)
                code.length > CODE_MAX_LENGTH -> Result.Error(PromocodeError.CreationFailure.CodeTooLong)
                else -> Result.Success(Unit)
            }

        fun validateDescription(description: String?): Result<Unit, PromocodeError.CreationFailure> {
            val sanitized = description?.let { sanitizeDescription(it) }
            return if (sanitized != null && sanitized.length > DESCRIPTION_MAX_LENGTH) {
                Result.Error(PromocodeError.CreationFailure.DescriptionTooLong)
            } else {
                Result.Success(Unit)
            }
        }

        fun validateMinimumOrderAmount(amount: Double): Result<Unit, PromocodeError.CreationFailure> =
            if (amount < MINIMUM_MONETARY_VALUE) {
                Result.Error(PromocodeError.CreationFailure.InvalidMinimumAmount)
            } else {
                Result.Success(Unit)
            }

        fun validateDateRange(
            startDate: Instant,
            endDate: Instant
        ): Result<Unit, PromocodeError.CreationFailure> =
            if (endDate <= startDate) {
                Result.Error(PromocodeError.CreationFailure.InvalidDateRange)
            } else {
                Result.Success(Unit)
            }

        @OptIn(ExperimentalTime::class)
        fun create(
            code: String,
            service: Service,
            author: User,
            discount: Discount,
            minimumOrderAmount: Double,
            startDate: Instant,
            endDate: Instant,
            isVerified: Boolean,
            description: String? = null,
            imageUrls: List<String> = emptyList()
        ): Result<Promocode, PromocodeError.CreationFailure> {
            val sanitizedCode = sanitizeCode(code)
            when (val result = validateCode(sanitizedCode)) {
                is Result.Error -> return result
                is Result.Success -> Unit
            }

            when (val result = validateMinimumOrderAmount(minimumOrderAmount)) {
                is Result.Error -> return result
                is Result.Success -> Unit
            }

            when (val result = discount.validate(minimumOrderAmount)) {
                is Result.Error -> return result
                is Result.Success -> Unit
            }

            val cleanDescription = description?.let { sanitizeDescription(it) }
            when (val result = validateDescription(cleanDescription)) {
                is Result.Error -> return result
                is Result.Success -> Unit
            }

            when (val result = validateDateRange(startDate, endDate)) {
                is Result.Error -> return result
                is Result.Success -> Unit
            }

            val cleanImageUrls = imageUrls.map { it.trim() }.filter { it.isNotBlank() }
            if (cleanImageUrls.size > MAX_IMAGES) {
                return Result.Error(PromocodeError.CreationFailure.TooManyImages)
            }

            val promoId = when (
                val idResult = PromocodeId.create(
                    serviceName = service.name,
                    code = sanitizedCode,
                )
            ) {
                is Result.Error -> return Result.Error(idResult.error)
                is Result.Success -> idResult.data
            }

            return Result.Success(
                Promocode(
                    id = promoId,
                    code = sanitizedCode,
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
                    imageUrls = cleanImageUrls,
                    createdAt = Clock.System.now(),
                    voteScore = 0,
                ),
            )
        }

        /**
         * Reconstruct a Promocode from storage/DTO (for mappers/repositories only).
         * Assumes data is already validated. No sanitization performed.
         */
        @OptIn(ExperimentalTime::class)
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
            imageUrls: List<String> = emptyList(),
            createdAt: Instant,
            voteScore: Int
        ): Promocode =
            Promocode(
                id = id,
                code = code,
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
                imageUrls = imageUrls,
                createdAt = createdAt,
                voteScore = voteScore,
            )
    }
}
