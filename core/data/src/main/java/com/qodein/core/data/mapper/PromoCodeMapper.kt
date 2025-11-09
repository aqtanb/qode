package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.dto.PromoCodeDto
import com.qodein.shared.model.Discount
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromocodeId
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

        val promoCodeId = PromocodeId(dto.documentId)
        val createdBy = if (dto.createdBy.isBlank()) {
            throw IllegalArgumentException("PromoCode createdBy cannot be blank")
        } else {
            UserId(dto.createdBy)
        }

        // Map discount based on type
        val discount = when (dto.type.lowercase()) {
            "percentage" -> Discount.Percentage(
                dto.discountPercentage
                    ?: throw IllegalArgumentException("Percentage promo code missing discountPercentage"),
            )
            "fixed", "fixed_amount" -> Discount.FixedAmount(
                dto.discountAmount
                    ?: throw IllegalArgumentException("Fixed amount promo code missing discountAmount"),
            )
            else -> throw IllegalArgumentException("Unknown promo code type: ${dto.type}")
        }

        return PromoCode(
            id = promoCodeId,
            code = dto.code,
            discount = discount,
            serviceId = dto.serviceId?.let { ServiceId(it) },
            serviceName = dto.serviceName,
            category = dto.category,
            description = dto.description,
            minimumOrderAmount = dto.minimumOrderAmount,
            startDate = dto.startDate.toInstant().toKotlinInstant(),
            endDate = dto.endDate.toInstant().toKotlinInstant(),
            isFirstUserOnly = dto.isFirstUserOnly,
            isOneTimeUseOnly = dto.isOneTimeUseOnly,
            upvotes = dto.upvotes,
            downvotes = dto.downvotes,
            shares = dto.shares,
            targetCountries = dto.targetCountries,
            isVerified = dto.isVerified,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
            createdBy = createdBy,
            createdByUsername = dto.createdByUsername,
            createdByAvatarUrl = dto.createdByAvatarUrl,
            serviceLogoUrl = dto.serviceLogoUrl,
        )
    }

    fun toDto(domain: PromoCode): PromoCodeDto {
        val (type, discountPercentage, discountAmount) = when (domain.discount) {
            is Discount.Percentage -> Triple("percentage", domain.discount.value, null)
            is Discount.FixedAmount -> Triple("fixed", null, domain.discount.value)
        }

        return PromoCodeDto(
            documentId = domain.id.value,
            code = domain.code,
            serviceId = domain.serviceId?.value,
            serviceName = domain.serviceName,
            category = domain.category,
            description = domain.description,
            type = type,
            discountPercentage = discountPercentage,
            discountAmount = discountAmount,
            minimumOrderAmount = domain.minimumOrderAmount,
            isFirstUserOnly = domain.isFirstUserOnly,
            isOneTimeUseOnly = domain.isOneTimeUseOnly,
            upvotes = domain.upvotes,
            downvotes = domain.downvotes,
            voteScore = domain.voteScore,
            shares = domain.shares,
            targetCountries = domain.targetCountries,
            isVerified = domain.isVerified,
            startDate = Timestamp(domain.startDate.toJavaInstant()),
            endDate = Timestamp(domain.endDate.toJavaInstant()),
            createdAt = Timestamp(domain.createdAt.toJavaInstant()),
            createdBy = domain.createdBy.value,
            createdByUsername = domain.createdByUsername,
            createdByAvatarUrl = domain.createdByAvatarUrl,
            serviceLogoUrl = domain.serviceLogoUrl,
        )
    }
}
