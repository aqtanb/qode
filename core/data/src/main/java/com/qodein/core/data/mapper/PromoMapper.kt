package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.PromoDto
import com.qodein.shared.model.Promo
import com.qodein.shared.model.PromoId
import com.qodein.shared.model.UserId
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant

object PromoMapper {

    fun toDomain(dto: PromoDto): Promo {
        require(dto.id.isNotBlank()) { "Promo ID cannot be blank" }
        require(dto.title.isNotBlank()) { "Promo title cannot be blank" }
        require(dto.description.isNotBlank()) { "Promo description cannot be blank" }
        require(dto.serviceName.isNotBlank()) { "Promo service name cannot be blank" }
        require(dto.createdBy.isNotBlank()) { "Promo createdBy cannot be blank" }

        return Promo(
            id = PromoId(dto.id),
            title = dto.title,
            description = dto.description,
            imageUrls = dto.imageUrls,
            serviceName = dto.serviceName,
            category = dto.category,
            targetCountries = dto.targetCountries,
            upvotes = dto.upvotes,
            downvotes = dto.downvotes,
            views = dto.views,
            shares = dto.shares,
            isVerified = dto.isVerified,
            createdBy = UserId(dto.createdBy),
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
            expiresAt = dto.expiresAt?.toInstant()?.toKotlinInstant(),
            isUpvotedByCurrentUser = dto.isUpvotedByCurrentUser,
            isDownvotedByCurrentUser = dto.isDownvotedByCurrentUser,
            isBookmarkedByCurrentUser = dto.isBookmarkedByCurrentUser,
        )
    }

    fun toDto(domain: Promo): PromoDto =
        PromoDto(
            id = domain.id.value,
            title = domain.title,
            description = domain.description,
            imageUrls = domain.imageUrls,
            serviceName = domain.serviceName,
            category = domain.category,
            targetCountries = domain.targetCountries,
            upvotes = domain.upvotes,
            downvotes = domain.downvotes,
            views = domain.views,
            shares = domain.shares,
            isVerified = domain.isVerified,
            createdBy = domain.createdBy.value,
            createdAt = Timestamp(domain.createdAt.toJavaInstant()),
            expiresAt = domain.expiresAt?.let { Timestamp(it.toJavaInstant()) },
            isUpvotedByCurrentUser = domain.isUpvotedByCurrentUser,
            isDownvotedByCurrentUser = domain.isDownvotedByCurrentUser,
            isBookmarkedByCurrentUser = domain.isBookmarkedByCurrentUser,
        )

    fun toDomainList(dtos: List<PromoDto>): List<Promo> =
        dtos.mapNotNull { dto ->
            try {
                toDomain(dto)
            } catch (e: Exception) {
                null
            }
        }

    fun toDtoList(promos: List<Promo>): List<PromoDto> = promos.map { toDto(it) }
}
