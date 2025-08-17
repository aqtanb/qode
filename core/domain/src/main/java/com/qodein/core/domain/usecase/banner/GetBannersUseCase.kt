package com.qodein.core.domain.usecase.banner

import com.qodein.core.domain.repository.BannerRepository
import com.qodein.core.domain.repository.DevicePreferencesRepository
import com.qodein.core.model.Banner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

/**
 * Use case for retrieving banners based on user's country and preferences.
 * Implements fallback strategies for better UX.
 */
class GetBannersUseCase @Inject constructor(
    private val bannerRepository: BannerRepository,
    private val devicePreferencesRepository: DevicePreferencesRepository
) {

    /**
     * Gets banners for the user's current country with intelligent fallbacks.
     *
     * @param limit Maximum number of banners to return
     * @return Flow of banners with automatic country detection and fallbacks
     */
    operator fun invoke(limit: Int = 10): Flow<List<Banner>> =
        try {
            getUserCountryCode()
                .catch { exception ->
                    // If getting country code fails, emit default country
                    emit(DEFAULT_COUNTRY_CODE)
                }
                .map { countryCode ->
                    // Return the repository flow directly for real-time updates
                    bannerRepository.getBannersForCountry(countryCode, limit)
                        .catch { exception ->
                            // Fallback to all active banners if country-specific fails
                            bannerRepository.getAllActiveBanners(limit)
                                .catch { fallbackException ->
                                    // Final fallback: emit default banners
                                    emit(getDefaultFallbackBanners())
                                }
                        }
                }
                .flatMapLatest { bannerFlow ->
                    bannerFlow
                }
        } catch (e: Exception) {
            // If everything fails, return default banners
            flowOf(getDefaultFallbackBanners())
        }

    /**
     * Gets banners for a specific country code.
     * Useful for testing or manual country selection.
     *
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @param limit Maximum number of banners to return
     * @return Flow of banners for the specified country
     */
    fun getBannersForCountry(
        countryCode: String,
        limit: Int = 10
    ): Flow<List<Banner>> =
        bannerRepository.getBannersForCountry(countryCode, limit)
            .catch { exception ->
                // Fallback to all active banners if country-specific fails
                bannerRepository.getAllActiveBanners(limit)
                    .catch { fallbackException ->
                        // Final fallback: emit default banners
                        emit(getDefaultFallbackBanners())
                    }
                    .collect { fallbackBanners ->
                        emit(fallbackBanners)
                    }
            }

    /**
     * Gets the user's country code from device preferences or system locale.
     * Implements fallback chain: saved preference → system locale → default
     */
    private fun getUserCountryCode(): Flow<String> =
        try {
            devicePreferencesRepository.getLanguage()
                .map { languageTag ->
                    // Try to extract country from language tag (e.g., "en-US" → "US")
                    extractCountryFromLanguageTag(languageTag.toString())
                        ?: getSystemCountryCode()
                        ?: DEFAULT_COUNTRY_CODE
                }
        } catch (e: Exception) {
            flowOf(getSystemCountryCode() ?: DEFAULT_COUNTRY_CODE)
        }

    /**
     * Extracts country code from language tag (e.g., "en-US" → "US")
     */
    private fun extractCountryFromLanguageTag(languageTag: String): String? =
        try {
            val locale = Locale.forLanguageTag(languageTag)
            locale.country.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }

    /**
     * Gets system default country code from device locale
     */
    private fun getSystemCountryCode(): String? =
        try {
            Locale.getDefault().country.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }

    /**
     * Creates default fallback banners when all else fails.
     * Returns empty list so only real banners from server are displayed.
     */
    private fun getDefaultFallbackBanners(): List<Banner> = emptyList()

    companion object {
        private const val DEFAULT_COUNTRY_CODE = "US" // Fallback country
    }
}
