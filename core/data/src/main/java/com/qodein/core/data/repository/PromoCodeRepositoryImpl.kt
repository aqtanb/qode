package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirebaseVoteDataSource
import com.qodein.core.data.datasource.FirestorePromoCodeDataSource
import com.qodein.core.data.datasource.FirestoreServiceDataSource
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.PromoCodeVote
import com.qodein.shared.model.Service
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromoCodeRepositoryImpl @Inject constructor(
    private val promoCodeDataSource: FirestorePromoCodeDataSource,
    private val serviceDataSource: FirestoreServiceDataSource,
    private val voteDataSource: FirebaseVoteDataSource
) : PromoCodeRepository {

    override fun createPromoCode(promoCode: PromoCode): Flow<PromoCode> =
        flow {
            emit(promoCodeDataSource.createPromoCode(promoCode))
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
            emit(promoCodeDataSource.getPromoCodeByCode(code))
        }

    override fun updatePromoCode(promoCode: PromoCode): Flow<PromoCode> =
        flow {
            emit(promoCodeDataSource.updatePromoCode(promoCode))
        }

    override fun deletePromoCode(id: PromoCodeId): Flow<Unit> =
        flow {
            promoCodeDataSource.deletePromoCode(id)
            emit(Unit)
        }

    override fun voteOnPromoCode(
        promoCodeId: PromoCodeId,
        userId: UserId,
        isUpvote: Boolean
    ): Flow<PromoCodeVote?> =
        flow {
            emit(voteDataSource.voteOnPromoCode(promoCodeId, userId, isUpvote))
        }

    override fun getUserVote(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): Flow<PromoCodeVote?> =
        flow {
            emit(voteDataSource.getUserVote(promoCodeId, userId))
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
            emit(promoCodeDataSource.addComment(promoCodeId, userId, comment))
        }

    override fun getPromoCodesByUser(
        userId: UserId,
        limit: Int,
        offset: Int
    ): Flow<List<PromoCode>> =
        flow {
            emit(promoCodeDataSource.getPromoCodesByUser(userId, limit, offset))
        }

    override fun getPromoCodesByService(serviceName: String): Flow<List<PromoCode>> =
        flow {
            emit(promoCodeDataSource.getPromoCodesByService(serviceName))
        }

    override fun getPromoCodeByCodeAndService(
        code: String,
        serviceName: String
    ): Flow<PromoCode?> =
        flow {
            emit(promoCodeDataSource.getPromoCodeByCodeAndService(code, serviceName))
        }

    override fun observePromoCodes(ids: List<PromoCodeId>): Flow<List<PromoCode>> = promoCodeDataSource.observePromoCodes(ids)

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
