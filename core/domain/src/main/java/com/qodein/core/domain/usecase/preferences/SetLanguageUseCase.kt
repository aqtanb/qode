package com.qodein.core.domain.usecase.preferences

import com.qodein.core.domain.repository.DevicePreferencesRepository
import com.qodein.core.model.Language
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for updating language preference.
 *
 * Handles language preference updates with proper error propagation.
 */
@Singleton
class SetLanguageUseCase @Inject constructor(private val devicePreferencesRepository: DevicePreferencesRepository) {

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
