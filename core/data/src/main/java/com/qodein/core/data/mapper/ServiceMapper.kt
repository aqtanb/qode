package com.qodein.core.data.mapper

import com.qodein.core.data.dto.ServiceDto
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId

object ServiceMapper {
    fun toDomain(dto: ServiceDto): Service =
        Service.fromDto(
            id = ServiceId(dto.documentId),
            name = dto.name,
            siteUrl = dto.siteUrl,
            logoUrl = dto.logoUrl,
            promoCodeCount = dto.promocodeCount,
        )

    fun toDto(service: Service): ServiceDto =
        ServiceDto(
            documentId = service.id.value,
            name = service.name,
            siteUrl = service.siteUrl,
            logoUrl = service.logoUrl,
            promocodeCount = service.promocodeCount,
        )
}
