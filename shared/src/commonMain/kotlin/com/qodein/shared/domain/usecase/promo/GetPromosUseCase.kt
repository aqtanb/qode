package com.qodein.shared.domain.usecase.promo

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoRepository
import com.qodein.shared.domain.repository.PromoSortBy
import com.qodein.shared.model.Promo
import kotlinx.coroutines.flow.Flow

class GetPromosUseCase constructor(private val promoRepository: PromoRepository) {
    operator fun invoke(
        query: String? = null,
        sortBy: PromoSortBy = PromoSortBy.POPULARITY,
        filterByService: String? = null,
        filterByCategory: String? = null,
        filterByCountry: String? = null,
        includeExpired: Boolean = false,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<Result<List<Promo>>> {
        require(limit in 1..100) { "Limit must be between 1 and 100" }
        require(offset >= 0) { "Offset must be non-negative" }

        val trimmedQuery = query?.trim()?.takeIf { it.isNotBlank() }
        val trimmedService = filterByService?.trim()?.takeIf { it.isNotBlank() }
        val trimmedCategory = filterByCategory?.trim()?.takeIf { it.isNotBlank() }
        val trimmedCountry = filterByCountry?.trim()?.uppercase()?.takeIf { it.isNotBlank() }

        return promoRepository.getPromos(
            query = trimmedQuery,
            sortBy = sortBy,
            filterByService = trimmedService,
            filterByCategory = trimmedCategory,
            filterByCountry = trimmedCountry,
            includeExpired = includeExpired,
            limit = limit,
            offset = offset,
        ).asResult()
    }
}
