package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Banner
import com.qodein.shared.model.BannerId
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing promotional banners.
 * Returns Result<D, OperationError> for type-safe error handling.
 * Repository implementations handle exception translation to domain errors.
 */
interface BannerRepository {

    /**
     * Gets all active banners for the specified country, sorted by priority.
     */
    fun getBannersForCountry(
        countryCode: String,
        limit: Int = 10
    ): Flow<Result<List<Banner>, OperationError>>

    /**
     * Gets all active banners regardless of country targeting, sorted by priority.
     */
    fun getAllActiveBanners(limit: Int = 10): Flow<Result<List<Banner>, OperationError>>

    /**
     * Gets a specific banner by ID.
     */
    suspend fun getBannerById(bannerId: BannerId): Result<Banner?, OperationError>

    /**
     * Observes real-time changes to a specific set of banners.
     */
    fun observeBanners(bannerIds: List<BannerId>): Flow<Result<List<Banner>, OperationError>>
}
