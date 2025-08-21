package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.model.PromoCode
import kotlinx.coroutines.flow.Flow

class SearchPromoCodesUseCase constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(
        query: String,
        sortBy: PromoCodeSortBy = PromoCodeSortBy.POPULARITY,
        filterByType: String? = null,
        filterByService: String? = null,
        filterByCategory: String? = null,
        isFirstUserOnly: Boolean? = null
    ): Flow<Result<List<PromoCode>>> =
        promoCodeRepository.getPromoCodes(
            query = query.trim(),
            sortBy = sortBy,
            filterByType = filterByType,
            filterByService = filterByService,
            filterByCategory = filterByCategory,
            isFirstUserOnly = isFirstUserOnly,
            limit = 50, // Higher limit for search results
        )
            .asResult()
}
