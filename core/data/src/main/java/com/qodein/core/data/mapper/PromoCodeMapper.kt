package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.PromoCodeDto
import com.qodein.core.data.model.PromoCodeUsageDto
import com.qodein.core.data.model.PromoCodeVoteDto
import com.qodein.core.model.PromoCode
import com.qodein.core.model.PromoCodeId
import com.qodein.core.model.PromoCodeUsage
import com.qodein.core.model.PromoCodeVote
import com.qodein.core.model.UserId
import java.time.Instant

/**
 * Mapper between PromoCode domain models and Firestore DTOs.
 * Handles conversion between different data representations.
 */
object PromoCodeMapper {

    fun toDomain(dto: PromoCodeDto): PromoCode {
        val promoCodeId = PromoCodeId(dto.id)
        val createdBy = dto.createdBy?.let { UserId(it) }

        return when (dto.type) {
            "percentage" -> PromoCode.PercentagePromoCode(
                id = promoCodeId,
                code = dto.code,
                serviceName = dto.serviceName,
                category = dto.category,
                title = dto.title,
                description = dto.description,
                discountPercentage = dto.discountPercentage
                    ?: throw IllegalArgumentException("Percentage promo code missing discountPercentage"),
                minimumOrderAmount = dto.minimumOrderAmount,
                maximumDiscount = dto.maximumDiscount
                    ?: throw IllegalArgumentException("Percentage promo code missing maximumDiscount"),
                startDate = dto.startDate?.toInstant(),
                endDate = dto.endDate?.toInstant(),
                usageLimit = dto.usageLimit,
                isFirstUserOnly = dto.isFirstUserOnly,
                upvotes = dto.upvotes,
                downvotes = dto.downvotes,
                views = dto.views,
                screenshotUrl = dto.screenshotUrl,
                comments = dto.comments,
                createdAt = dto.createdAt?.toInstant() ?: Instant.now(),
                updatedAt = dto.updatedAt?.toInstant() ?: Instant.now(),
                createdBy = createdBy,
            )

            "fixed" -> PromoCode.FixedAmountPromoCode(
                id = promoCodeId,
                code = dto.code,
                serviceName = dto.serviceName,
                category = dto.category,
                title = dto.title,
                description = dto.description,
                discountAmount = dto.discountAmount
                    ?: throw IllegalArgumentException("Fixed amount promo code missing discountAmount"),
                minimumOrderAmount = dto.minimumOrderAmount,
                startDate = dto.startDate?.toInstant(),
                endDate = dto.endDate?.toInstant(),
                usageLimit = dto.usageLimit,
                isFirstUserOnly = dto.isFirstUserOnly,
                upvotes = dto.upvotes,
                downvotes = dto.downvotes,
                views = dto.views,
                screenshotUrl = dto.screenshotUrl,
                comments = dto.comments,
                createdAt = dto.createdAt?.toInstant() ?: Instant.now(),
                updatedAt = dto.updatedAt?.toInstant() ?: Instant.now(),
                createdBy = createdBy,
            )

            "promo" -> PromoCode.PromoPromoCode(
                id = promoCodeId,
                code = dto.code,
                serviceName = dto.serviceName,
                category = dto.category,
                title = dto.title,
                description = dto.description
                    ?: throw IllegalArgumentException("Promo promo code missing description"),
                startDate = dto.startDate?.toInstant(),
                endDate = dto.endDate?.toInstant(),
                usageLimit = dto.usageLimit,
                isFirstUserOnly = dto.isFirstUserOnly,
                upvotes = dto.upvotes,
                downvotes = dto.downvotes,
                views = dto.views,
                screenshotUrl = dto.screenshotUrl,
                comments = dto.comments,
                createdAt = dto.createdAt?.toInstant() ?: Instant.now(),
                updatedAt = dto.updatedAt?.toInstant() ?: Instant.now(),
                createdBy = createdBy,
            )

            else -> throw IllegalArgumentException("Unknown promo code type: ${dto.type}")
        }
    }

    fun toDto(domain: PromoCode): PromoCodeDto =
        when (domain) {
            is PromoCode.PercentagePromoCode -> PromoCodeDto(
                id = domain.id.value,
                code = domain.code,
                serviceName = domain.serviceName,
                category = domain.category,
                title = domain.title,
                description = domain.description,
                type = "percentage",
                discountPercentage = domain.discountPercentage,
                minimumOrderAmount = domain.minimumOrderAmount,
                maximumDiscount = domain.maximumDiscount,
                usageLimit = domain.usageLimit,
                isFirstUserOnly = domain.isFirstUserOnly,
                upvotes = domain.upvotes,
                downvotes = domain.downvotes,
                views = domain.views,
                screenshotUrl = domain.screenshotUrl,
                comments = domain.comments,
                startDate = domain.startDate?.let { Timestamp(it.epochSecond, it.nano) },
                endDate = domain.endDate?.let { Timestamp(it.epochSecond, it.nano) },
                createdAt = Timestamp(domain.createdAt.epochSecond, domain.createdAt.nano),
                updatedAt = Timestamp(domain.updatedAt.epochSecond, domain.updatedAt.nano),
                createdBy = domain.createdBy?.value,
            )

            is PromoCode.FixedAmountPromoCode -> PromoCodeDto(
                id = domain.id.value,
                code = domain.code,
                serviceName = domain.serviceName,
                category = domain.category,
                title = domain.title,
                description = domain.description,
                type = "fixed",
                discountAmount = domain.discountAmount,
                minimumOrderAmount = domain.minimumOrderAmount,
                usageLimit = domain.usageLimit,
                isFirstUserOnly = domain.isFirstUserOnly,
                upvotes = domain.upvotes,
                downvotes = domain.downvotes,
                views = domain.views,
                screenshotUrl = domain.screenshotUrl,
                comments = domain.comments,
                startDate = domain.startDate?.let { Timestamp(it.epochSecond, it.nano) },
                endDate = domain.endDate?.let { Timestamp(it.epochSecond, it.nano) },
                createdAt = Timestamp(domain.createdAt.epochSecond, domain.createdAt.nano),
                updatedAt = Timestamp(domain.updatedAt.epochSecond, domain.updatedAt.nano),
                createdBy = domain.createdBy?.value,
            )

            is PromoCode.PromoPromoCode -> PromoCodeDto(
                id = domain.id.value,
                code = domain.code,
                serviceName = domain.serviceName,
                category = domain.category,
                title = domain.title,
                description = domain.description,
                type = "promo",
                usageLimit = domain.usageLimit,
                isFirstUserOnly = domain.isFirstUserOnly,
                upvotes = domain.upvotes,
                downvotes = domain.downvotes,
                views = domain.views,
                screenshotUrl = domain.screenshotUrl,
                comments = domain.comments,
                startDate = domain.startDate?.let { Timestamp(it.epochSecond, it.nano) },
                endDate = domain.endDate?.let { Timestamp(it.epochSecond, it.nano) },
                createdAt = Timestamp(domain.createdAt.epochSecond, domain.createdAt.nano),
                updatedAt = Timestamp(domain.updatedAt.epochSecond, domain.updatedAt.nano),
                createdBy = domain.createdBy?.value,
            )
        }

    fun voteToDomain(dto: PromoCodeVoteDto): PromoCodeVote =
        PromoCodeVote(
            id = dto.id,
            promoCodeId = PromoCodeId(dto.promoCodeId),
            userId = UserId(dto.userId),
            isUpvote = dto.isUpvote,
            votedAt = dto.votedAt?.toInstant() ?: Instant.now(),
        )

    fun voteToDto(domain: PromoCodeVote): PromoCodeVoteDto =
        PromoCodeVoteDto(
            id = domain.id,
            promoCodeId = domain.promoCodeId.value,
            userId = domain.userId.value,
            isUpvote = domain.isUpvote,
            votedAt = Timestamp(domain.votedAt.epochSecond, domain.votedAt.nano),
        )

    fun usageToDomain(dto: PromoCodeUsageDto): PromoCodeUsage =
        PromoCodeUsage(
            id = dto.id,
            promoCodeId = PromoCodeId(dto.promoCodeId),
            userId = UserId(dto.userId),
            orderAmount = dto.orderAmount,
            discountAmount = dto.discountAmount,
            usedAt = dto.usedAt?.toInstant() ?: Instant.now(),
        )

    fun usageToDto(domain: PromoCodeUsage): PromoCodeUsageDto =
        PromoCodeUsageDto(
            id = domain.id,
            promoCodeId = domain.promoCodeId.value,
            userId = domain.userId.value,
            orderAmount = domain.orderAmount,
            discountAmount = domain.discountAmount,
            usedAt = Timestamp(domain.usedAt.epochSecond, domain.usedAt.nano),
        )

    private fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(seconds, nanoseconds.toLong())
}
