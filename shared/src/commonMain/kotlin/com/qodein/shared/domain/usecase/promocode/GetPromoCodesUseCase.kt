package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import kotlinx.coroutines.flow.Flow

class GetPromoCodesUseCase constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(
        query: String? = null,
        sortBy: PromoCodeSortBy = PromoCodeSortBy.POPULARITY,
        filterByServices: List<String>? = null,
        filterByCategories: List<String>? = null,
        paginationRequest: PaginationRequest = PaginationRequest.firstPage()
    ): Flow<Result<PaginatedResult<PromoCode>>> =
        promoCodeRepository.getPromoCodes(
            query = query,
            sortBy = sortBy,
            filterByServices = filterByServices,
            filterByCategories = filterByCategories,
            paginationRequest = paginationRequest,
        )
            .asResult()
}
