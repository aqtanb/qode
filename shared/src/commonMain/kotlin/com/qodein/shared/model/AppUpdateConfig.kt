package com.qodein.shared.model

data class AppUpdateConfig(
    val minimumVersionCode: Int = DEFAULT_MINIMUM_VERSION_CODE,
    val flexibleUpdateStaleDays: Int = DEFAULT_FLEXIBLE_UPDATE_STALE_DAYS
) {
    companion object {
        const val DEFAULT_MINIMUM_VERSION_CODE = 0
        const val DEFAULT_FLEXIBLE_UPDATE_STALE_DAYS = 3
    }
}
