package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.dto.DiscountDto
import com.qodein.core.data.dto.PromocodeDto
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.ServiceId
import com.qodein.shared.model.UserId
import kotlin.time.Clock
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

object PromocodeMapper {

    fun toDomain(dto: PromocodeDto): Promocode {
        val discount = when (dto.discount.type) {
            DiscountDto.TYPE_FIXED_AMOUNT -> Discount.FixedAmount(dto.discount.value ?: 0.0)
            DiscountDto.TYPE_PERCENTAGE -> Discount.Percentage(dto.discount.value ?: 0.0)
            else -> Discount.Percentage(dto.discount.value ?: 0.0) // default to percentage on unknown
        }

        return Promocode.fromDto(
            id = PromocodeId(dto.documentId),
            code = dto.code,
            discount = discount,
            minimumOrderAmount = dto.minimumOrderAmount,
            startDate = dto.startDate.toInstant().toKotlinInstant(),
            endDate = dto.endDate.toInstant().toKotlinInstant(),
            authorId = UserId(dto.authorId),
            serviceName = dto.serviceName,
            description = dto.description,
            isFirstUserOnly = dto.isFirstUserOnly,
            isOneTimeUseOnly = dto.isOneTimeUseOnly,
            upvotes = dto.upvotes,
            downvotes = dto.downvotes,
            isVerified = dto.isVerified,
            serviceId = dto.serviceId?.let { ServiceId(it) },
            serviceLogoUrl = dto.serviceLogoUrl,
            authorUsername = dto.authorUsername,
            authorAvatarUrl = dto.authorAvatarUrl,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
        )
    }

    fun toDto(domain: Promocode): PromocodeDto {
        val discountDto = when (domain.discount) {
            is Discount.Percentage -> DiscountDto(type = DiscountDto.TYPE_PERCENTAGE, value = domain.discount.value)
            is Discount.FixedAmount -> DiscountDto(type = DiscountDto.TYPE_FIXED_AMOUNT, value = domain.discount.value)
        }

        return PromocodeDto(
            documentId = domain.id.value,
            code = domain.code.value,
            startDate = Timestamp(domain.startDate.toJavaInstant()),
            endDate = Timestamp(domain.endDate.toJavaInstant()),
            serviceName = domain.serviceName,
            discount = discountDto,
            minimumOrderAmount = domain.minimumOrderAmount,
            description = domain.description,
            serviceId = domain.serviceId?.value,
            serviceLogoUrl = domain.serviceLogoUrl,
            isFirstUserOnly = domain.isFirstUseOnly,
            isOneTimeUseOnly = domain.isOneTimeUseOnly,
            isVerified = domain.isVerified,
            upvotes = domain.upvotes,
            downvotes = domain.downvotes,
            authorId = domain.authorId.value,
            authorUsername = domain.authorUsername,
            authorAvatarUrl = domain.authorAvatarUrl,
            createdAt = Timestamp(domain.createdAt.toJavaInstant()),
        )
    }
}
