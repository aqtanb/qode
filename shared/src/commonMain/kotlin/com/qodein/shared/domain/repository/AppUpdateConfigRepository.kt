package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.AppUpdateConfig

interface AppUpdateConfigRepository {
    suspend fun getAppUpdateConfig(): Result<AppUpdateConfig, OperationError>
}
