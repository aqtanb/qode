package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.model.PromoCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

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
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
