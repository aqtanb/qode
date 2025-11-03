package com.qodein.core.data.repository

import co.touchlab.kermit.Logger
import com.qodein.core.data.datasource.FirestorePromocodeDataSource
import com.qodein.core.data.datasource.FirestoreServiceDataSource
import com.qodein.core.data.datasource.FirestoreUserDataSource
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PromoCodeError
import com.qodein.shared.common.error.ServiceError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.Service
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class PromocodeRepositoryImpl constructor(
    private val promoCodeDataSource: FirestorePromocodeDataSource,
    private val serviceDataSource: FirestoreServiceDataSource,
    private val userDataSource: FirestoreUserDataSource
) : PromocodeRepository {

    companion object {
        private const val TAG = "PromocodeRepository"
    }

    override fun createPromoCode(promoCode: PromoCode): Flow<Result<PromoCode, OperationError>> =
        flow {
            Logger.i(TAG) { "Repository creating promo code: ${promoCode.code}" }
            try {
                val result = promoCodeDataSource.createPromoCode(promoCode)
                Logger.i(TAG) { "Repository successfully created promo code: ${result.id.value}" }

                userDataSource.incrementPromocodeCount(promoCode.createdBy.value)

                emit(Result.Success(result))
            } catch (e: SecurityException) {
                Logger.e(TAG, e) { "Repository failed to create promo code - unauthorized: ${promoCode.code}" }
                emit(Result.Error(PromoCodeError.SubmissionFailure.NotAuthorized))
            } catch (e: IllegalArgumentException) {
                Logger.e(TAG, e) { "Repository failed to create promo code - invalid data: ${promoCode.code}" }
                emit(Result.Error(PromoCodeError.SubmissionFailure.InvalidData))
            } catch (e: IOException) {
                Logger.e(TAG, e) { "Repository failed to create promo code - network: ${promoCode.code}" }
                emit(Result.Error(SystemError.Offline))
            } catch (e: IllegalStateException) {
                Logger.e(TAG, e) { "Repository failed to create promo code - service down: ${promoCode.code}" }
                emit(Result.Error(SystemError.ServiceDown))
            } catch (e: Exception) {
                Logger.e(TAG, e) { "Repository failed to create promo code - unknown: ${promoCode.code}" }
                emit(Result.Error(SystemError.Unknown))
            }
        }

    override fun getPromoCodes(
        query: String?,
        sortBy: ContentSortBy,
        filterByServices: List<String>?,
        filterByCategories: List<String>?,
        paginationRequest: PaginationRequest<ContentSortBy>
    ): Flow<Result<PaginatedResult<PromoCode, ContentSortBy>, OperationError>> =
        flow {
            try {
                val result = promoCodeDataSource.getPromoCodes(
                    query = query,
                    sortBy = sortBy,
                    filterByServices = filterByServices,
                    filterByCategories = filterByCategories,
                    paginationRequest = paginationRequest,
                )
                emit(Result.Success(result))
            } catch (e: IOException) {
                emit(Result.Error(SystemError.Offline))
            } catch (e: IllegalStateException) {
                emit(Result.Error(SystemError.ServiceDown))
            } catch (e: Exception) {
                emit(Result.Error(PromoCodeError.RetrievalFailure.NotFound))
            }
        }

    override suspend fun getPromoCodeById(id: PromoCodeId): Result<PromoCode, OperationError> =
        try {
            val promoCode = promoCodeDataSource.getPromoCodeById(id)
            if (promoCode != null) {
                Result.Success(promoCode)
            } else {
                Logger.w(TAG) { "PromoCode not found: ${id.value}" }
                Result.Error(PromoCodeError.RetrievalFailure.NotFound)
            }
        } catch (e: IllegalArgumentException) {
            Logger.e(TAG, e) { "Invalid PromoCode data for id: ${id.value}" }
            Result.Error(PromoCodeError.RetrievalFailure.NotFound)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error getting PromoCode: ${id.value}" }
            Result.Error(SystemError.Offline)
        } catch (e: IllegalStateException) {
            Logger.e(TAG, e) { "Service down getting PromoCode: ${id.value}" }
            Result.Error(SystemError.ServiceDown)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Unknown error getting PromoCode: ${id.value}" }
            Result.Error(SystemError.Unknown)
        }

    // Service-related methods

    override fun searchServices(
        query: String,
        limit: Int
    ): Flow<Result<List<Service>, OperationError>> =
        flow {
            try {
                val result = serviceDataSource.searchServices(query, limit)
                emit(Result.Success(result))
            } catch (e: IOException) {
                emit(Result.Error(SystemError.Offline))
            } catch (e: Exception) {
                emit(Result.Error(ServiceError.SearchFailure.NoResults))
            }
        }

    override fun getPopularServices(limit: Int): Flow<Result<List<Service>, OperationError>> =
        flow {
            try {
                val result = serviceDataSource.getPopularServices(limit)
                emit(Result.Success(result))
            } catch (e: IOException) {
                emit(Result.Error(SystemError.Offline))
            } catch (e: Exception) {
                emit(Result.Error(ServiceError.RetrievalFailure.NotFound))
            }
        }
}
