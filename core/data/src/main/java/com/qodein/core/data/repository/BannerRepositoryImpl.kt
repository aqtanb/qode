package com.qodein.core.data.repository

import co.touchlab.kermit.Logger
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.qodein.core.data.datasource.FirestoreBannerDataSource
import com.qodein.core.data.mapper.BannerMapper
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.model.Banner

class BannerRepositoryImpl(private val dataSource: FirestoreBannerDataSource) : BannerRepository {

    companion object {
        private const val TAG = "BannerRepository"
    }

    override suspend fun getBanners(limit: Long): Result<List<Banner>, OperationError> =
        try {
            val rawBanners = dataSource.getBanners(limit)
            val domainBanners = rawBanners.map { BannerMapper.toDomain(it) }
            Logger.d(TAG) { "Successfully fetched ${domainBanners.size} banners" }
            Result.Success(domainBanners)
        } catch (e: FirebaseFirestoreException) {
            Result.Error(ErrorMapper.mapFirestoreException(e, TAG))
        } catch (e: FirebaseNetworkException) {
            Logger.e(TAG, e) { "Network error - no internet connection" }
            Result.Error(FirestoreError.Unavailable)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Unexpected error fetching banners: ${e.javaClass.simpleName}" }
            Result.Error(SystemError.Unknown)
        }
}
