package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestorePromoCodeDataSource
import com.qodein.core.domain.repository.PromoCodeRepository
import com.qodein.core.domain.repository.PromoCodeSortBy
import com.qodein.core.model.PromoCode
import com.qodein.core.model.PromoCodeId
import com.qodein.core.model.PromoCodeUsage
import com.qodein.core.model.PromoCodeVote
import com.qodein.core.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromoCodeRepositoryImpl @Inject constructor(private val dataSource: FirestorePromoCodeDataSource) : PromoCodeRepository {

    override fun createPromoCode(promoCode: PromoCode): Flow<PromoCode> =
        flow {
            emit(dataSource.createPromoCode(promoCode))
        }

    override fun getPromoCodes(
        query: String?,
        sortBy: PromoCodeSortBy,
        filterByType: String?,
        filterByService: String?,
        filterByCategory: String?,
        isFirstUserOnly: Boolean?,
        limit: Int,
        offset: Int
    ): Flow<List<PromoCode>> =
        flow {
            emit(
                dataSource.getPromoCodes(
                    query = query,
                    sortBy = sortBy,
                    filterByType = filterByType,
                    filterByService = filterByService,
                    filterByCategory = filterByCategory,
                    isFirstUserOnly = isFirstUserOnly,
                    limit = limit,
                    offset = offset,
                ),
            )
        }

    override fun getPromoCodeById(id: PromoCodeId): Flow<PromoCode?> =
        flow {
            emit(dataSource.getPromoCodeById(id))
        }

    override fun getPromoCodeByCode(code: String): Flow<PromoCode?> =
        flow {
            emit(dataSource.getPromoCodeByCode(code))
        }

    override fun updatePromoCode(promoCode: PromoCode): Flow<PromoCode> =
        flow {
            emit(dataSource.updatePromoCode(promoCode))
        }

    override fun deletePromoCode(id: PromoCodeId): Flow<Unit> =
        flow {
            dataSource.deletePromoCode(id)
            emit(Unit)
        }

    override fun voteOnPromoCode(
        promoCodeId: PromoCodeId,
        userId: UserId,
        isUpvote: Boolean
    ): Flow<PromoCodeVote> =
        flow {
            emit(dataSource.voteOnPromoCode(promoCodeId, userId, isUpvote))
        }

    override fun removeVote(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): Flow<Unit> =
        flow {
            dataSource.removeVote(promoCodeId, userId)
            emit(Unit)
        }

    override fun getUserVote(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): Flow<PromoCodeVote?> =
        flow {
            emit(dataSource.getUserVote(promoCodeId, userId))
        }

    override fun incrementViewCount(id: PromoCodeId): Flow<Unit> =
        flow {
            dataSource.incrementViewCount(id)
            emit(Unit)
        }

    override fun recordUsage(usage: PromoCodeUsage): Flow<PromoCodeUsage> =
        flow {
            emit(dataSource.recordUsage(usage))
        }

    override fun getUsageStatistics(promoCodeId: PromoCodeId): Flow<List<PromoCodeUsage>> =
        flow {
            emit(dataSource.getUsageStatistics(promoCodeId))
        }

    override fun addComment(
        promoCodeId: PromoCodeId,
        userId: UserId,
        comment: String
    ): Flow<PromoCode> =
        flow {
            emit(dataSource.addComment(promoCodeId, userId, comment))
        }

    override fun getPromoCodesByUser(
        userId: UserId,
        limit: Int,
        offset: Int
    ): Flow<List<PromoCode>> =
        flow {
            emit(dataSource.getPromoCodesByUser(userId, limit, offset))
        }

    override fun observePromoCodes(ids: List<PromoCodeId>): Flow<List<PromoCode>> = dataSource.observePromoCodes(ids)
}
