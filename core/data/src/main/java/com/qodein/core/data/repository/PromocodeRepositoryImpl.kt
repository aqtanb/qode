package com.qodein.core.data.repository

import co.touchlab.kermit.Logger
import com.google.firebase.firestore.FirebaseFirestoreException
import com.qodein.core.data.datasource.FirestorePromocodeDataSource
import com.qodein.core.data.datasource.FirestoreUserDataSource
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PromoCodeError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromocodeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class PromocodeRepositoryImpl constructor(
    private val promoCodeDataSource: FirestorePromocodeDataSource,
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

                // Side effect: increment user's promocode count
                try {
                    userDataSource.incrementPromocodeCount(promoCode.createdBy.value)
                    Logger.d(TAG) { "Incremented promocode count for user: ${promoCode.createdBy.value}" }
                } catch (e: Exception) {
                    Logger.w(TAG, e) { "Failed to increment promocode count for user: ${promoCode.createdBy.value} - ${e.message}" }
                    // Don't fail the main operation if side effect fails
                }

                emit(Result.Success(result))
            } catch (e: FirebaseFirestoreException) {
                emit(Result.Error(ErrorMapper.mapFirestoreException(e, TAG)))
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
                Logger.d(TAG) { "Getting promocodes: query=$query, sortBy=$sortBy" }
                val result = promoCodeDataSource.getPromoCodes(
                    query = query,
                    sortBy = sortBy,
                    filterByServices = filterByServices,
                    filterByCategories = filterByCategories,
                    paginationRequest = paginationRequest,
                )
                Logger.i(TAG) { "Retrieved ${result.data.size} promocodes" }
                emit(Result.Success(result))
            } catch (e: FirebaseFirestoreException) {
                emit(Result.Error(ErrorMapper.mapFirestoreException(e, TAG)))
            } catch (e: IOException) {
                Logger.e(TAG, e) { "Network error getting promocodes: ${e.message}" }
                emit(Result.Error(SystemError.Offline))
            } catch (e: IllegalStateException) {
                Logger.e(TAG, e) { "Service down getting promocodes: ${e.message}" }
                emit(Result.Error(SystemError.ServiceDown))
            } catch (e: Exception) {
                Logger.e(TAG, e) { "Unknown error getting promocodes: ${e::class.simpleName} - ${e.message}" }
                emit(Result.Error(PromoCodeError.RetrievalFailure.NotFound))
            }
        }

    override suspend fun getPromoCodeById(id: PromocodeId): Result<PromoCode, OperationError> =
        try {
            Logger.d(TAG) { "Getting promocode by id: ${id.value}" }
            val promoCode = promoCodeDataSource.getPromoCodeById(id)
            if (promoCode != null) {
                Logger.i(TAG) { "Found promocode: ${id.value}" }
                Result.Success(promoCode)
            } else {
                Logger.w(TAG) { "PromoCode not found: ${id.value}" }
                Result.Error(PromoCodeError.RetrievalFailure.NotFound)
            }
        } catch (e: FirebaseFirestoreException) {
            ErrorMapper.mapFirestoreException(e, TAG)
            Result.Error(PromoCodeError.RetrievalFailure.NotFound)
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
            Logger.e(TAG, e) { "Unknown error getting PromoCode: ${id.value} - ${e::class.simpleName}" }
            Result.Error(SystemError.Unknown)
        }
}
