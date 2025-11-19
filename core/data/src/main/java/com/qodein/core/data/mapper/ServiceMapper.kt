package com.qodein.core.data.mapper

import com.qodein.core.data.dto.ServiceDto
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId

object ServiceMapper {
    fun toDomain(dto: ServiceDto): Service =
        Service.fromDto(
            id = ServiceId(dto.documentId),
            name = dto.name,
            logoUrl = dto.logoUrl,
            promoCodeCount = dto.promoCodeCount,
        )
}
