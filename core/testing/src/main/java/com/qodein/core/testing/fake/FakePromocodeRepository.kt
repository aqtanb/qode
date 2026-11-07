package com.qodein.core.testing.fake

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.DatabaseError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.PromocodeSortBy
import com.qodein.shared.model.UserId

class FakePromocodeRepository : PromocodeRepository {
    val promocodes = mutableMapOf<PromocodeId, Promocode>()

    /** When set, createPromocode fails with this error. */
    var createError: OperationError? = null

    /** When set, getPromocodeById fails with this error instead of looking up [promocodes]. */
    var lookupError: OperationError? = null

    override suspend fun createPromocode(promocode: Promocode): Result<Unit, OperationError> {
        createError?.let { return Result.Error(it) }
        promocodes[promocode.id] = promocode
        return Result.Success(Unit)
    }

    override suspend fun getPromocodes(
        sortBy: PromocodeSortBy,
        filterByServices: List<String>?,
        paginationRequest: PaginationRequest<PromocodeSortBy>,
        blockedUserIds: Set<UserId>
    ): Result<PaginatedResult<Promocode, PromocodeSortBy>, OperationError> = error("Not used in tests")

    override suspend fun getPromocodeById(id: PromocodeId): Result<Promocode, OperationError> {
        lookupError?.let { return Result.Error(it) }
        return promocodes[id]?.let { Result.Success(it) } ?: Result.Error(DatabaseError.NotFound)
    }

    override suspend fun getPromocodesByUser(
        userId: UserId,
        cursor: Any?,
        limit: Int
    ): Result<PaginatedResult<Promocode, PromocodeSortBy>, OperationError> = error("Not used in tests")
}
