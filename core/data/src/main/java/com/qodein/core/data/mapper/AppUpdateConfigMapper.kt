package com.qodein.core.data.mapper

import com.qodein.core.data.dto.AppUpdateConfigDto
import com.qodein.shared.model.AppUpdateConfig

object AppUpdateConfigMapper {
    fun toDomain(dto: AppUpdateConfigDto): AppUpdateConfig =
        AppUpdateConfig(
            minimumVersionCode = dto.minimumVersionCode,
            flexibleUpdateStaleDays = dto.flexibleUpdateStaleDays,
        )
}
