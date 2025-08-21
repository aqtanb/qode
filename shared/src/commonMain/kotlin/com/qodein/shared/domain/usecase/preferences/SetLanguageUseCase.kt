package com.qodein.shared.domain.usecase.preferences

import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Language

/**
 * Use case for updating language preference.
 *
 * Handles language preference updates with proper error propagation.
 */

class SetLanguageUseCase constructor(private val devicePreferencesRepository: DevicePreferencesRepository) {

    /**
     * Set language preference
     *
     * @param language The language to set
     * @throws java.io.IOException when storage operation fails
     */
    suspend operator fun invoke(language: Language) {
        devicePreferencesRepository.setLanguage(language)
    }
}
