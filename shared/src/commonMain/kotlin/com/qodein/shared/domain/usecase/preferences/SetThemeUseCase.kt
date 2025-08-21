package com.qodein.shared.domain.usecase.preferences

import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Theme

/**
 * Use case for updating theme preference.
 *
 * Handles theme preference updates with proper error propagation.
 */

class SetThemeUseCase constructor(private val devicePreferencesRepository: DevicePreferencesRepository) {

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
