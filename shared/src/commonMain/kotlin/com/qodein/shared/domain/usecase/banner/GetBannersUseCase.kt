package com.qodein.shared.domain.usecase.banner

import co.touchlab.kermit.Logger
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.model.Banner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * Use case for retrieving banners based on user's country and preferences.
 * Implements fallback strategies for better UX.
 */
class GetBannersUseCase constructor(private val bannerRepository: BannerRepository) {

    companion object {
        private val logger = Logger.withTag("GetBannersUseCase")
        private const val DEFAULT_COUNTRY_CODE = "KZ" // Kazakhstan as fallback country for KZ market
    }

    init {
        logger.d { "GetBannersUseCase constructor called - DI working correctly" }
    }

    /**
     * Gets banners for Kazakhstan market with intelligent fallbacks.
     *
     * @param limit Maximum number of banners to return
     * @return Flow<Result<List<Banner>>> with automatic fallbacks and error handling
     */
    operator fun invoke(limit: Int = 10): Flow<Result<List<Banner>>> = getBannersForCountry(DEFAULT_COUNTRY_CODE, limit)

    /**
     * Gets banners for a specific country code.
     * Useful for testing or manual country selection.
     *
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @param limit Maximum number of banners to return
     * @return Flow<Result<List<Banner>>> for the specified country
     */
    fun getBannersForCountry(
        countryCode: String,
        limit: Int = 10
    ): Flow<Result<List<Banner>>> {
        logger.d { "getBannersForCountry called with countryCode=$countryCode, limit=$limit" }
        return bannerRepository.getAllActiveBanners(limit)
            .map { banners ->
                logger.d { "Received ${banners.size} banners from repository" }
                // Filter expired banners and prioritize country-specific ones
                val filtered = filterExpiredBanners(banners)
                val countryBanners = filtered.filter { banner ->
                    banner.targetCountries.isEmpty() ||
                        // Global banners
                        banner.targetCountries.contains(countryCode.uppercase()) // Country-specific
                }
                val result = countryBanners.ifEmpty { filtered.ifEmpty { getDefaultFallbackBanners() } }
                logger.d { "Returning ${result.size} banners after filtering" }
                result
            }
            .asResult()
    }

    /**
     * Filters banners that have expired based on current server time.
     * Since we can't get exact server time, we use client time as approximation.
     */
    private fun filterExpiredBanners(banners: List<Banner>): List<Banner> {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return banners.filter { banner ->
            banner.isDisplayable(currentTime)
        }
    }

    /**
     * Creates default fallback banners when all else fails.
     * Returns empty list so only real banners from server are displayed.
     */
    private fun getDefaultFallbackBanners(): List<Banner> = emptyList()
}
