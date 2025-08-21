package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestoreBannerDataSource
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.model.Banner
import com.qodein.shared.model.BannerId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of BannerRepository using Firestore as the data source.
 * Follows the existing repository pattern in the project.
 */
@Singleton
class BannerRepositoryImpl @Inject constructor(private val firestoreBannerDataSource: FirestoreBannerDataSource) : BannerRepository {

    override fun getBannersForCountry(
        countryCode: String,
        limit: Int
    ): Flow<List<Banner>> = firestoreBannerDataSource.getBannersForCountry(countryCode, limit)

    override fun getAllActiveBanners(limit: Int): Flow<List<Banner>> = firestoreBannerDataSource.getAllActiveBanners(limit)

    override suspend fun getBannerById(bannerId: BannerId): Banner? = firestoreBannerDataSource.getBannerById(bannerId)

    override fun observeBanners(bannerIds: List<BannerId>): Flow<List<Banner>> = firestoreBannerDataSource.observeBanners(bannerIds)
}
