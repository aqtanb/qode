package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Banner

interface BannerRepository {
    suspend fun getBanners(limit: Long): Result<List<Banner>, OperationError>
}
