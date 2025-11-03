package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestoreBannerDataSource
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.model.Banner
import com.qodein.shared.model.BannerId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

/**
 * Implementation of BannerRepository using Firestore as the data source.
 */

class BannerRepositoryImpl constructor(private val firestoreBannerDataSource: FirestoreBannerDataSource) : BannerRepository {

    override fun getBannersForCountry(
        countryCode: String,
        limit: Int
    ): Flow<Result<List<Banner>, OperationError>> =
        flow {
            try {
                firestoreBannerDataSource.getBannersForCountry(countryCode, limit).collect { banners ->
                    emit(Result.Success(banners))
                }
            } catch (e: IOException) {
                emit(Result.Error(SystemError.Offline))
            } catch (e: IllegalStateException) {
                emit(Result.Error(SystemError.ServiceDown))
            } catch (e: Exception) {
                emit(Result.Error(SystemError.Unknown))
            }
        }

    override fun getAllActiveBanners(limit: Int): Flow<Result<List<Banner>, OperationError>> =
        flow {
            try {
                firestoreBannerDataSource.getAllActiveBanners(limit).collect { banners ->
                    emit(Result.Success(banners))
                }
            } catch (e: IOException) {
                emit(Result.Error(SystemError.Offline))
            } catch (e: IllegalStateException) {
                emit(Result.Error(SystemError.ServiceDown))
            } catch (e: Exception) {
                emit(Result.Error(SystemError.Unknown))
            }
        }

    override suspend fun getBannerById(bannerId: BannerId): Result<Banner?, OperationError> =
        try {
            val banner = firestoreBannerDataSource.getBannerById(bannerId)
            Result.Success(banner)
        } catch (e: IOException) {
            Result.Error(SystemError.Offline)
        } catch (e: IllegalStateException) {
            Result.Error(SystemError.ServiceDown)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }

    override fun observeBanners(bannerIds: List<BannerId>): Flow<Result<List<Banner>, OperationError>> =
        flow {
            try {
                firestoreBannerDataSource.observeBanners(bannerIds).collect { banners ->
                    emit(Result.Success(banners))
                }
            } catch (e: IOException) {
                emit(Result.Error(SystemError.Offline))
            } catch (e: IllegalStateException) {
                emit(Result.Error(SystemError.ServiceDown))
            } catch (e: Exception) {
                emit(Result.Error(SystemError.Unknown))
            }
        }
}
