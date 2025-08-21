package com.qodein.shared.domain.usecase.banner

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.model.Banner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * Use case for retrieving banners based on user's country and preferences.
 * Implements fallback strategies for better UX.
 */
class GetBannersUseCase constructor(private val bannerRepository: BannerRepository) {

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
    ): Flow<Result<List<Banner>>> =
        bannerRepository.getBannersForCountry(countryCode, limit)
            .map { banners -> filterExpiredBanners(banners) }
            .catch { exception ->
                // Fallback to all active banners if country-specific fails
                bannerRepository.getAllActiveBanners(limit)
                    .map { banners -> filterExpiredBanners(banners) }
                    .catch { fallbackException ->
                        // Final fallback: emit default banners
                        emit(getDefaultFallbackBanners())
                    }
                    .collect { fallbackBanners ->
                        emit(fallbackBanners)
                    }
            }
            .asResult()

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

    companion object {
        private const val DEFAULT_COUNTRY_CODE = "KZ" // Kazakhstan as fallback country for KZ market
    }
}
