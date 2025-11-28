package com.qodein.core.data.mapper

import com.qodein.core.data.dto.BannerDto
import com.qodein.shared.model.Banner
import com.qodein.shared.model.BannerId

object BannerMapper {
    fun toDomain(dto: BannerDto): Banner =
        Banner(
            id = BannerId(dto.documentId),
            imageUrl = dto.imageUrl,
            ctaTitle = dto.ctaTitle,
            ctaDescription = dto.ctaDescription,
            ctaUrl = dto.ctaUrl,
            priority = dto.priority,
        )
}
