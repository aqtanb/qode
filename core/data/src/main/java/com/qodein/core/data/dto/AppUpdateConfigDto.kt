package com.qodein.core.data.dto

import com.qodein.shared.model.AppUpdateConfig

data class AppUpdateConfigDto(
    val minimumVersionCode: Int = AppUpdateConfig.DEFAULT_MINIMUM_VERSION_CODE,
    val flexibleUpdateStaleDays: Int = AppUpdateConfig.DEFAULT_FLEXIBLE_UPDATE_STALE_DAYS
) {
    companion object {
        const val COLLECTION_NAME = "app_config"
        const val FIELD_UPDATE_POLICY = "update_policy"
    }
}
