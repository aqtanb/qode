package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestorePromoDataSource
import com.qodein.shared.domain.repository.PromoRepository
import com.qodein.shared.domain.repository.PromoSortBy
import com.qodein.shared.model.Promo
import com.qodein.shared.model.PromoId
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromoRepositoryImpl @Inject constructor(private val dataSource: FirestorePromoDataSource) : PromoRepository {

    override fun createPromo(promo: Promo): Flow<Promo> =
        flow {
            emit(dataSource.createPromo(promo))
        }

    override fun getPromos(
        query: String?,
        sortBy: PromoSortBy,
        filterByService: String?,
        filterByCategory: String?,
        filterByCountry: String?,
        includeExpired: Boolean,
        limit: Int,
        offset: Int
    ): Flow<List<Promo>> =
        flow {
            emit(
                dataSource.getPromos(
                    query = query,
                    sortBy = sortBy,
                    filterByService = filterByService,
                    filterByCategory = filterByCategory,
                    filterByCountry = filterByCountry,
                    includeExpired = includeExpired,
                    limit = limit,
                    offset = offset,
                ),
            )
        }

    override fun getPromoById(id: PromoId): Flow<Promo?> =
        flow {
            emit(dataSource.getPromoById(id))
        }

    override fun updatePromo(promo: Promo): Flow<Promo> =
        flow {
            emit(dataSource.updatePromo(promo))
        }

    override fun deletePromo(
        id: PromoId,
        createdBy: UserId
    ): Flow<Unit> =
        flow {
            dataSource.deletePromo(id, createdBy)
            emit(Unit)
        }

    override fun voteOnPromo(
        promoId: PromoId,
        userId: UserId,
        isUpvote: Boolean
    ): Flow<Promo> =
        flow {
            emit(dataSource.voteOnPromo(promoId, userId, isUpvote))
        }

    override fun removePromoVote(
        promoId: PromoId,
        userId: UserId
    ): Flow<Promo> =
        flow {
            emit(dataSource.removePromoVote(promoId, userId))
        }

    override fun bookmarkPromo(
        promoId: PromoId,
        userId: UserId,
        isBookmarked: Boolean
    ): Flow<Promo> =
        flow {
            emit(dataSource.bookmarkPromo(promoId, userId, isBookmarked))
        }

    override fun incrementViewCount(id: PromoId): Flow<Unit> =
        flow {
            dataSource.incrementViewCount(id)
            emit(Unit)
        }

    override fun incrementShareCount(id: PromoId): Flow<Unit> =
        flow {
            dataSource.incrementShareCount(id)
            emit(Unit)
        }

    override fun getPromosByUser(
        userId: UserId,
        limit: Int,
        offset: Int
    ): Flow<List<Promo>> =
        flow {
            emit(dataSource.getPromosByUser(userId, limit, offset))
        }

    override fun getPromosByService(
        serviceName: String,
        limit: Int,
        offset: Int
    ): Flow<List<Promo>> =
        flow {
            emit(dataSource.getPromosByService(serviceName, limit, offset))
        }

    override fun getPromosByCategory(
        category: String,
        limit: Int,
        offset: Int
    ): Flow<List<Promo>> =
        flow {
            emit(dataSource.getPromosByCategory(category, limit, offset))
        }

    override fun getPromosByCountry(
        countryCode: String,
        limit: Int,
        offset: Int
    ): Flow<List<Promo>> =
        flow {
            emit(dataSource.getPromosByCountry(countryCode, limit, offset))
        }

    override fun getBookmarkedPromos(
        userId: UserId,
        limit: Int,
        offset: Int
    ): Flow<List<Promo>> =
        flow {
            emit(dataSource.getBookmarkedPromos(userId, limit, offset))
        }

    override fun getExpiringPromos(
        daysAhead: Int,
        limit: Int
    ): Flow<List<Promo>> =
        flow {
            emit(dataSource.getExpiringPromos(daysAhead, limit))
        }

    override fun getTrendingPromos(
        timeWindow: Int,
        limit: Int
    ): Flow<List<Promo>> =
        flow {
            emit(dataSource.getTrendingPromos(timeWindow, limit))
        }

    override fun getVerifiedPromos(
        limit: Int,
        offset: Int
    ): Flow<List<Promo>> =
        flow {
            emit(dataSource.getVerifiedPromos(limit, offset))
        }

    override fun observePromos(ids: List<PromoId>): Flow<List<Promo>> = dataSource.observePromos(ids)
}
