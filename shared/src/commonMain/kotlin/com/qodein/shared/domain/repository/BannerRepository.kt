package com.qodein.shared.domain.repository

import com.qodein.shared.model.Banner
import com.qodein.shared.model.BannerId
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing promotional banners.
 * Follows the existing architecture pattern with clean error handling.
 */
interface BannerRepository {

    /**
     * Gets all active banners for the specified country, sorted by priority.
     *
     * @param countryCode ISO 3166-1 alpha-2 country code (e.g., "KZ", "US")
     * @param limit Maximum number of banners to return
     * @return Flow of banners that will update in real-time
     * @throws IOException for network connectivity issues
     * @throws IllegalStateException for server/data issues
     */
    fun getBannersForCountry(
        countryCode: String,
        limit: Int = 10
    ): Flow<List<Banner>>

    /**
     * Gets all active banners regardless of country targeting, sorted by priority.
     * Used as fallback when country detection fails.
     *
     * @param limit Maximum number of banners to return
     * @return Flow of banners that will update in real-time
     * @throws IOException for network connectivity issues
     * @throws IllegalStateException for server/data issues
     */
    fun getAllActiveBanners(limit: Int = 10): Flow<List<Banner>>

    /**
     * Gets a specific banner by ID.
     *
     * @param bannerId The unique identifier of the banner
     * @return The banner if found, null otherwise
     * @throws IOException for network connectivity issues
     * @throws IllegalStateException for server/data issues
     */
    suspend fun getBannerById(bannerId: BannerId): Banner?

    /**
     * Observes real-time changes to a specific set of banners.
     * Useful for tracking banner updates without re-fetching all data.
     *
     * @param bannerIds List of banner IDs to observe
     * @return Flow of banners that will update when any banner changes
     */
    fun observeBanners(bannerIds: List<BannerId>): Flow<List<Banner>>
}
