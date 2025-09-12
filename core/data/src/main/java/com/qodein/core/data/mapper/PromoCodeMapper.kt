package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.PromoCodeDto
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.ServiceId
import com.qodein.shared.model.UserId
import kotlin.time.Clock
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

/**
 * Mapper between PromoCode domain models and Firestore DTOs.
 * Handles conversion between different data representations.
 */
object PromoCodeMapper {

    fun toDomain(dto: PromoCodeDto): PromoCode {
        // Validate required fields first
        if (dto.documentId.isBlank()) {
            throw IllegalArgumentException("PromoCode documentId cannot be blank")
        }
        if (dto.code.isBlank()) {
            throw IllegalArgumentException("PromoCode code cannot be blank")
        }
        if (dto.serviceName.isBlank()) {
            throw IllegalArgumentException("PromoCode serviceName cannot be blank")
        }
        if (dto.type.isBlank()) {
            throw IllegalArgumentException("PromoCode type cannot be blank")
        }

        val promoCodeId = PromoCodeId(dto.documentId)
        val createdBy = dto.createdBy?.let { UserId(it) }

        return when (dto.type.lowercase()) {
            "percentage" -> PromoCode.PercentagePromoCode(
                id = promoCodeId,
                code = dto.code,
                serviceId = dto.serviceId?.let { ServiceId(it) },
                serviceName = dto.serviceName,
                category = dto.category,
                description = dto.description,
                discountPercentage = dto.discountPercentage
                    ?: throw IllegalArgumentException("Percentage promo code missing discountPercentage"),
                minimumOrderAmount = dto.minimumOrderAmount,
                startDate = dto.startDate.toInstant().toKotlinInstant(),
                endDate = dto.endDate.toInstant().toKotlinInstant(),
                isFirstUserOnly = dto.isFirstUserOnly,
                upvotes = dto.upvotes,
                downvotes = dto.downvotes,
                views = dto.views,
                shares = dto.shares,
                targetCountries = dto.targetCountries,
                isVerified = dto.isVerified,
                createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
                createdBy = createdBy,
                isUpvotedByCurrentUser = dto.isUpvotedByCurrentUser,
                isDownvotedByCurrentUser = dto.isDownvotedByCurrentUser,
                isBookmarkedByCurrentUser = dto.isBookmarkedByCurrentUser,
            )

            "fixed", "fixed_amount" -> PromoCode.FixedAmountPromoCode(
                id = promoCodeId,
                code = dto.code,
                serviceId = dto.serviceId?.let { ServiceId(it) },
                serviceName = dto.serviceName,
                category = dto.category,
                description = dto.description,
                discountAmount = dto.discountAmount
                    ?: throw IllegalArgumentException("Fixed amount promo code missing discountAmount"),
                minimumOrderAmount = dto.minimumOrderAmount,
                startDate = dto.startDate.toInstant().toKotlinInstant(),
                endDate = dto.endDate.toInstant().toKotlinInstant(),
                isFirstUserOnly = dto.isFirstUserOnly,
                upvotes = dto.upvotes,
                downvotes = dto.downvotes,
                views = dto.views,
                shares = dto.shares,
                targetCountries = dto.targetCountries,
                isVerified = dto.isVerified,
                createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
                createdBy = createdBy,
                isUpvotedByCurrentUser = dto.isUpvotedByCurrentUser,
                isDownvotedByCurrentUser = dto.isDownvotedByCurrentUser,
                isBookmarkedByCurrentUser = dto.isBookmarkedByCurrentUser,
            )

            else -> throw IllegalArgumentException("Unknown promo code type: ${dto.type}")
        }
    }

    fun toDto(domain: PromoCode): PromoCodeDto =
        when (domain) {
            is PromoCode.PercentagePromoCode -> PromoCodeDto(
                documentId = domain.id.value,
                code = domain.code,
                serviceId = domain.serviceId?.value,
                serviceName = domain.serviceName,
                category = domain.category,
                description = domain.description,
                type = "percentage",
                discountPercentage = domain.discountPercentage,
                minimumOrderAmount = domain.minimumOrderAmount,
                isFirstUserOnly = domain.isFirstUserOnly,
                upvotes = domain.upvotes,
                downvotes = domain.downvotes,
                views = domain.views,
                shares = domain.shares,
                targetCountries = domain.targetCountries,
                isVerified = domain.isVerified,
                startDate = domain.startDate.let { Timestamp(it.toJavaInstant()) },
                endDate = domain.endDate.let { Timestamp(it.toJavaInstant()) },
                createdAt = Timestamp(domain.createdAt.toJavaInstant()),
                createdBy = domain.createdBy?.value,
                isUpvotedByCurrentUser = domain.isUpvotedByCurrentUser,
                isDownvotedByCurrentUser = domain.isDownvotedByCurrentUser,
                isBookmarkedByCurrentUser = domain.isBookmarkedByCurrentUser,
            )

            is PromoCode.FixedAmountPromoCode -> PromoCodeDto(
                documentId = domain.id.value,
                code = domain.code,
                serviceId = domain.serviceId?.value,
                serviceName = domain.serviceName,
                category = domain.category,
                description = domain.description,
                type = "fixed",
                discountAmount = domain.discountAmount,
                minimumOrderAmount = domain.minimumOrderAmount,
                isFirstUserOnly = domain.isFirstUserOnly,
                upvotes = domain.upvotes,
                downvotes = domain.downvotes,
                views = domain.views,
                shares = domain.shares,
                targetCountries = domain.targetCountries,
                isVerified = domain.isVerified,
                startDate = domain.startDate.let { Timestamp(it.toJavaInstant()) },
                endDate = domain.endDate.let { Timestamp(it.toJavaInstant()) },
                createdAt = Timestamp(domain.createdAt.toJavaInstant()),
                createdBy = domain.createdBy?.value,
                isUpvotedByCurrentUser = domain.isUpvotedByCurrentUser,
                isDownvotedByCurrentUser = domain.isDownvotedByCurrentUser,
                isBookmarkedByCurrentUser = domain.isBookmarkedByCurrentUser,
            )
        }
}
