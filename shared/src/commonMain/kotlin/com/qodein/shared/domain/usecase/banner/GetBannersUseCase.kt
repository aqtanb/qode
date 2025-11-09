package com.qodein.shared.domain.usecase.banner

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.model.Banner
import com.qodein.shared.model.Language

class GetBannersUseCase(private val repository: BannerRepository) {
    companion object {
        private const val DEFAULT_LIMIT = 10L
    }

    suspend operator fun invoke(
        userLanguage: Language? = null,
        limit: Long = DEFAULT_LIMIT
    ): Result<List<Banner>, OperationError> = repository.getBanners(limit)
}
