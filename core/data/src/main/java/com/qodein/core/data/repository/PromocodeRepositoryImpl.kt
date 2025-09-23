package com.qodein.core.data.repository

import co.touchlab.kermit.Logger
import com.qodein.core.data.datasource.FirestorePromocodeDataSource
import com.qodein.core.data.datasource.FirestoreServiceDataSource
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.Service
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromocodeRepositoryImpl @Inject constructor(
    private val promoCodeDataSource: FirestorePromocodeDataSource,
    private val serviceDataSource: FirestoreServiceDataSource
) : PromocodeRepository {

    companion object {
        private const val TAG = "PromocodeRepository"
    }

    override fun createPromoCode(promoCode: PromoCode): Flow<PromoCode> =
        flow {
            Logger.i(TAG) { "Repository creating promo code: ${promoCode.code}" }
            try {
                val result = promoCodeDataSource.createPromoCode(promoCode)
                Logger.i(TAG) { "Repository successfully created promo code: ${result.id.value}" }
                emit(result)
            } catch (e: Exception) {
                Logger.e(TAG, e) { "Repository failed to create promo code: ${promoCode.code}" }
                when (e) {
                    is IOException -> throw e // Re-throw IOException as-is
                    is SecurityException -> throw e // Re-throw security exceptions
                    else -> throw IOException("Failed to create promo code in Firestore: ${e.message}", e)
                }
            }
        }

    override fun getPromoCodes(
        query: String?,
        sortBy: ContentSortBy,
        filterByServices: List<String>?,
        filterByCategories: List<String>?,
        paginationRequest: PaginationRequest
    ): Flow<PaginatedResult<PromoCode>> =
        flow {
            emit(
                promoCodeDataSource.getPromoCodes(
                    query = query,
                    sortBy = sortBy,
                    filterByServices = filterByServices,
                    filterByCategories = filterByCategories,
                    paginationRequest = paginationRequest,
                ),
            )
        }

    override fun getPromoCodeById(id: PromoCodeId): Flow<PromoCode?> =
        flow {
            emit(promoCodeDataSource.getPromoCodeById(id))
        }

    override fun getPromoCodeByCode(code: String): Flow<PromoCode?> =
        flow {
            throw NotImplementedError("getPromoCodeByCode not implemented in data source")
        }

    override fun updatePromoCode(promoCode: PromoCode): Flow<PromoCode> =
        flow {
            emit(promoCodeDataSource.updatePromoCode(promoCode))
        }

    override fun deletePromoCode(id: PromoCodeId): Flow<Unit> =
        flow {
            throw NotImplementedError("deletePromoCode not implemented in data source")
        }

    override fun incrementViewCount(id: PromoCodeId): Flow<Unit> =
        flow {
            promoCodeDataSource.incrementViewCount(id)
            emit(Unit)
        }

    override fun addComment(
        promoCodeId: PromoCodeId,
        userId: UserId,
        comment: String
    ): Flow<PromoCode> =
        flow {
            throw NotImplementedError("addComment not implemented in data source")
        }

    override fun getPromoCodesByUser(
        userId: UserId,
        limit: Int,
        offset: Int
    ): Flow<List<PromoCode>> =
        flow {
            throw NotImplementedError("getPromoCodesByUser not implemented in data source")
        }

    override fun getPromoCodesByService(serviceName: String): Flow<List<PromoCode>> =
        flow {
            throw NotImplementedError("getPromoCodesByService not implemented in data source")
        }

    override fun getPromoCodeByCodeAndService(
        code: String,
        serviceName: String
    ): Flow<PromoCode?> =
        flow {
            throw NotImplementedError("getPromoCodeByCodeAndService not implemented in data source")
        }

    override fun observePromoCodes(ids: List<PromoCodeId>): Flow<List<PromoCode>> =
        flow {
            throw NotImplementedError("observePromoCodes not implemented in data source")
        }

    // Service-related methods

    override fun searchServices(
        query: String,
        limit: Int
    ): Flow<List<Service>> =
        flow {
            emit(serviceDataSource.searchServices(query, limit))
        }

    override fun getPopularServices(limit: Int): Flow<List<Service>> =
        flow {
            emit(serviceDataSource.getPopularServices(limit))
        }

    override fun getServicesByCategory(
        category: String,
        limit: Int
    ): Flow<List<Service>> =
        flow {
            emit(serviceDataSource.getServicesByCategory(category, limit))
        }
}
