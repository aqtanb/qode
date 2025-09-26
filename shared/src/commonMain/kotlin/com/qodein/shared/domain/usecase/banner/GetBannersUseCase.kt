package com.qodein.shared.domain.usecase.banner

import co.touchlab.kermit.Logger
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.map
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.model.Banner
import com.qodein.shared.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/**
 * Use case for retrieving banners based on user's country and preferences.
 * Implements fallback strategies and language prioritization for better UX.
 */
class GetBannersUseCase(private val bannerRepository: BannerRepository) {

    companion object {
        private val logger = Logger.withTag("GetBannersUseCase")
        private const val DEFAULT_COUNTRY_CODE = "KZ" // Kazakhstan as fallback country for KZ market
    }

    init {
        logger.d { "GetBannersUseCase constructor called - DI working correctly" }
    }

    /**
     * Gets banners with intelligent filtering and prioritization.
     *
     * @param countryCode ISO 3166-1 alpha-2 country code (defaults to KZ)
     * @param userLanguage Optional user language for banner prioritization
     * @param limit Maximum number of banners to return
     * @return Flow<Result<List<Banner>>> with filtering, prioritization and fallbacks
     */
    fun getBanners(
        countryCode: String = DEFAULT_COUNTRY_CODE,
        userLanguage: Language? = null,
        limit: Int = 5
    ): Flow<Result<List<Banner>, OperationError>> {
        logger.d { "getBanners called with countryCode=$countryCode, language=$userLanguage, limit=$limit" }
        return bannerRepository.getAllActiveBanners(limit)
            .map { result ->
                result.map { banners ->
                    logger.d { "Received ${banners.size} banners from repository" }

                    // Step 1: Filter expired banners
                    val validBanners = filterExpiredBanners(banners)

                    // Step 2: Filter by country (global + country-specific)
                    val countryBanners = filterBannersByCountry(validBanners, countryCode)

                    // Step 3: Apply language prioritization if specified
                    val finalResult = if (userLanguage != null) {
                        prioritizeBannersByLanguage(countryBanners, userLanguage)
                    } else {
                        countryBanners
                    }

                    // Step 4: Apply fallback logic
                    val finalResult2 = when {
                        finalResult.isNotEmpty() -> finalResult
                        countryBanners.isNotEmpty() -> countryBanners
                        validBanners.isNotEmpty() -> validBanners
                        else -> getDefaultFallbackBanners()
                    }

                    logger.d { "Returning ${finalResult2.size} banners after filtering and prioritization" }
                    finalResult2
                }
            }
    }

    /**
     * Convenience operator function for default Kazakhstan market.
     */
    operator fun invoke(limit: Int = 5): Flow<Result<List<Banner>, OperationError>> = getBanners(limit = limit)

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
     * Filters banners by country targeting.
     * Includes global banners (no country targeting) and country-specific banners.
     */
    private fun filterBannersByCountry(
        banners: List<Banner>,
        countryCode: String
    ): List<Banner> =
        banners.filter { banner ->
            banner.targetCountries.isEmpty() ||
                // Global banners
                banner.targetCountries.contains(countryCode.uppercase()) // Country-specific
        }

    /**
     * Prioritizes banners by language availability.
     * Banners with content in the user's preferred language appear first.
     */
    private fun prioritizeBannersByLanguage(
        banners: List<Banner>,
        userLanguage: Language
    ): List<Banner> {
        val languageKey = when (userLanguage) {
            Language.ENGLISH -> "en"
            Language.RUSSIAN -> "ru"
            Language.KAZAKH -> "kk"
        }

        // Separate banners into those with and without user's language
        val bannersWithUserLanguage = banners.filter { banner ->
            banner.ctaTitle.containsKey(languageKey) && banner.ctaDescription.containsKey(languageKey)
        }

        val bannersWithoutUserLanguage = banners.filter { banner ->
            !banner.ctaTitle.containsKey(languageKey) || !banner.ctaDescription.containsKey(languageKey)
        }

        // Return banners with user's language first, followed by others
        return bannersWithUserLanguage + bannersWithoutUserLanguage
    }

    /**
     * Creates default fallback banners when all else fails.
     * Returns empty list so only real banners from server are displayed.
     */
    private fun getDefaultFallbackBanners(): List<Banner> = emptyList()
}
