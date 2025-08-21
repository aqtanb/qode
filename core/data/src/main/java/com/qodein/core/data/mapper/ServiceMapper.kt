package com.qodein.core.data.mapper

import com.qodein.core.data.model.ServiceDto
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId
import kotlinx.datetime.Instant

object ServiceMapper {

    fun toDomain(dto: ServiceDto): Service =
        Service(
            id = ServiceId(dto.id),
            name = dto.name,
            category = dto.category,
            logoUrl = dto.logoUrl,
            isPopular = dto.isPopular,
            createdAt = Instant.fromEpochSeconds(dto.createdAt),
        )

    fun toDto(service: Service): ServiceDto =
        ServiceDto(
            id = service.id.value,
            name = service.name,
            category = service.category,
            logoUrl = service.logoUrl,
            isPopular = service.isPopular,
            createdAt = service.createdAt.epochSeconds,
        )

    fun toDomainList(dtos: List<ServiceDto>): List<Service> = dtos.map { toDomain(it) }

    fun toDtoList(services: List<Service>): List<ServiceDto> = services.map { toDto(it) }
}
