package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import kotlinx.coroutines.flow.Flow

class GetPromocodesUseCase(private val promoCodeRepository: PromocodeRepository) {
    operator fun invoke(
        query: String? = null,
        sortBy: ContentSortBy = ContentSortBy.POPULARITY,
        filterByServices: List<String>? = null,
        filterByCategories: List<String>? = null,
        paginationRequest: PaginationRequest<ContentSortBy> = PaginationRequest.firstPage()
    ): Flow<Result<PaginatedResult<PromoCode, ContentSortBy>, OperationError>> =
        promoCodeRepository.getPromoCodes(
            query = query,
            sortBy = sortBy,
            filterByServices = filterByServices,
            filterByCategories = filterByCategories,
            paginationRequest = paginationRequest,
        )
}
