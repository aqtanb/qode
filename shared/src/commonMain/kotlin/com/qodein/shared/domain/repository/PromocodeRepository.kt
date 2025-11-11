package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromocodeId
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for PromoCode operations.
 * Returns Result<D, OperationError> for type-safe error handling.
 * Repository implementations handle exception translation to domain errors.
 */
interface PromocodeRepository {

    /**
     * Create a new promo code.
     */
    fun createPromoCode(promoCode: PromoCode): Flow<Result<PromoCode, OperationError>>

    /**
     * Get promo codes with filtering and sorting using cursor-based pagination.
     */
    fun getPromoCodes(
        query: String? = null,
        sortBy: ContentSortBy = ContentSortBy.POPULARITY,
        filterByServices: List<String>? = null,
        filterByCategories: List<String>? = null,
        paginationRequest: PaginationRequest<ContentSortBy> = PaginationRequest.firstPage()
    ): Flow<Result<PaginatedResult<PromoCode, ContentSortBy>, OperationError>>

    /**
     * Get a specific promo code by ID.
     * Returns NotFound error if promo code doesn't exist.
     */
    suspend fun getPromoCodeById(id: PromocodeId): Result<PromoCode, OperationError>
}
