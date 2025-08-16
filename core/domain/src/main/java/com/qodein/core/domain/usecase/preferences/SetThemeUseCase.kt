package com.qodein.core.domain.usecase.preferences

import com.qodein.core.domain.repository.DevicePreferencesRepository
import com.qodein.core.model.Theme
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for updating theme preference.
 *
 * Handles theme preference updates with proper error propagation.
 */
@Singleton
class SetThemeUseCase @Inject constructor(private val devicePreferencesRepository: DevicePreferencesRepository) {

    /**
     * Set theme preference
     *
     * @param theme The theme to set
     * @throws java.io.IOException when storage operation fails
     */
    suspend operator fun invoke(theme: Theme) {
        devicePreferencesRepository.setTheme(theme)
    }
}
