package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.ServiceDto
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId
import kotlin.time.Instant
import kotlin.time.toKotlinInstant

object ServiceMapper {

    fun toDomain(dto: ServiceDto): Service =
        Service(
            id = ServiceId(dto.documentId),
            name = dto.name,
            category = dto.category,
            logoUrl = dto.logoUrl,
            isPopular = dto.isPopular,
            promoCodeCount = dto.promoCodeCount,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Instant.fromEpochSeconds(0),
        )

    fun toDto(service: Service): ServiceDto =
        ServiceDto(
            documentId = service.id.value,
            name = service.name,
            category = service.category,
            logoUrl = service.logoUrl,
            isPopular = service.isPopular,
            promoCodeCount = service.promoCodeCount,
            createdAt = Timestamp(service.createdAt.epochSeconds, 0),
        )

    fun toDomainList(dtos: List<ServiceDto>): List<Service> = dtos.map { toDomain(it) }

    fun toDtoList(services: List<Service>): List<ServiceDto> = services.map { toDto(it) }
}
