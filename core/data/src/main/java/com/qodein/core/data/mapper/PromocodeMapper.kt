package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.dto.PromocodeAuthorDto
import com.qodein.core.data.dto.PromocodeDiscountDto
import com.qodein.core.data.dto.PromocodeDto
import com.qodein.core.data.dto.PromocodeEngagementDto
import com.qodein.core.data.dto.PromocodeServiceDto
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
            PromocodeDiscountDto.TYPE_FIXED_AMOUNT -> Discount.FixedAmount(dto.discount.value ?: 0.0)
            PromocodeDiscountDto.TYPE_PERCENTAGE -> Discount.Percentage(dto.discount.value ?: 0.0)
            PromocodeDiscountDto.TYPE_FREE_ITEM -> Discount.FreeItem(dto.discount.freeItemDescription ?: "")
            else -> Discount.Percentage(dto.discount.value ?: 0.0)
        }

        return Promocode.fromDto(
            id = PromocodeId(dto.documentId),
            code = dto.code,
            discount = discount,
            minimumOrderAmount = dto.minimumOrderAmount,
            startDate = dto.startDate.toInstant().toKotlinInstant(),
            endDate = dto.endDate.toInstant().toKotlinInstant(),
            authorId = UserId(dto.author.id),
            serviceName = dto.service.name,
            description = dto.description,
            upvotes = dto.engagement.upvotes,
            downvotes = dto.engagement.downvotes,
            isVerified = dto.isVerified,
            serviceId = dto.service.id?.let { ServiceId(it) },
            serviceLogoUrl = dto.service.logoUrl,
            serviceSiteUrl = dto.service.siteUrl,
            authorUsername = dto.author.username,
            authorAvatarUrl = dto.author.avatarUrl,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
        )
    }

    fun toDto(domain: Promocode): PromocodeDto {
        val discount = domain.discount
        val promocodeDiscountDto = when (discount) {
            is Discount.Percentage -> PromocodeDiscountDto(
                type = PromocodeDiscountDto.TYPE_PERCENTAGE,
                value = discount.value,
            )
            is Discount.FixedAmount -> PromocodeDiscountDto(
                type = PromocodeDiscountDto.TYPE_FIXED_AMOUNT,
                value = discount.value,
            )
            is Discount.FreeItem -> PromocodeDiscountDto(
                type = PromocodeDiscountDto.TYPE_FREE_ITEM,
                freeItemDescription = discount.description,
            )
        }

        return PromocodeDto(
            documentId = domain.id.value,
            code = domain.code.value,
            startDate = Timestamp(domain.startDate.toJavaInstant()),
            endDate = Timestamp(domain.endDate.toJavaInstant()),
            service = PromocodeServiceDto(
                id = domain.serviceId?.value,
                name = domain.serviceName,
                logoUrl = domain.serviceLogoUrl,
                siteUrl = domain.serviceSiteUrl,
            ),
            discount = promocodeDiscountDto,
            minimumOrderAmount = domain.minimumOrderAmount,
            description = domain.description,
            engagement = PromocodeEngagementDto(
                upvotes = domain.upvotes,
                downvotes = domain.downvotes,
                voteScore = domain.voteScore,
            ),
            author = PromocodeAuthorDto(
                id = domain.authorId.value,
                username = domain.authorUsername,
                avatarUrl = domain.authorAvatarUrl,
            ),
            isVerified = domain.isVerified,
            createdAt = Timestamp(domain.createdAt.toJavaInstant()),
        )
    }
}
