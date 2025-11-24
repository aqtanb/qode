package com.qodein.core.data.repository

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
import timber.log.Timber

class BannerRepositoryImpl(private val dataSource: FirestoreBannerDataSource) : BannerRepository {

    companion object {
        private const val TAG = "BannerRepository"
    }

    override suspend fun getBanners(limit: Long): Result<List<Banner>, OperationError> =
        try {
            val rawBanners = dataSource.getBanners(limit)
            val domainBanners = rawBanners.map { BannerMapper.toDomain(it) }
            Timber.d("Successfully fetched %d banners", domainBanners.size)
            Result.Success(domainBanners)
        } catch (e: FirebaseFirestoreException) {
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "Network error - no internet connection")
            Result.Error(FirestoreError.Unavailable)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error fetching banners: %s", e.javaClass.simpleName)
            Result.Error(SystemError.Unknown)
        }
}
