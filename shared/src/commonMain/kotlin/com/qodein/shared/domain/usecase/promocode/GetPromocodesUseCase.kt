package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.domain.repository.ReportRepository
import com.qodein.shared.domain.usecase.user.GetBlockedUserIdsUseCase
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Promocode
import kotlinx.coroutines.flow.first

class GetPromocodesUseCase(
    private val promocodeRepository: PromocodeRepository,
    private val reportRepository: ReportRepository,
    private val getBlockedUserIdsUseCase: GetBlockedUserIdsUseCase
) {
    companion object {
        const val DEFAULT_LIMIT = 5
    }
    suspend operator fun invoke(
        sortBy: ContentSortBy = ContentSortBy.POPULARITY,
        filterByServices: List<String>? = null,
        paginationRequest: PaginationRequest<ContentSortBy> = PaginationRequest.firstPage(DEFAULT_LIMIT)
    ): Result<PaginatedResult<Promocode, ContentSortBy>, OperationError> {
        val promocodes = promocodeRepository.getPromocodes(
            sortBy = sortBy,
            filterByServices = filterByServices,
            paginationRequest = paginationRequest,
        )
        return when (promocodes) {
            is Result.Error -> promocodes
            is Result.Success -> {
                val hiddenIds = reportRepository.getHiddenContentIds(ContentType.PROMO_CODE).first()
                val blockedUserIds = getBlockedUserIdsUseCase().first()

                val filteredPromocodes = promocodes.data.data.filterNot { promocode ->
                    promocode.id.value in hiddenIds ||
                        promocode.authorId.value in blockedUserIds
                }

                Result.Success(
                    PaginatedResult.of(
                        data = filteredPromocodes,
                        nextCursor = promocodes.data.nextCursor,
                        hasMore = promocodes.data.hasMore,
                    ),
                )
            }
        }
    }
}
