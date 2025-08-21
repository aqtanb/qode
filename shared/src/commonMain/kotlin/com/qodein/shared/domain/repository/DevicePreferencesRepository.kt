package com.qodein.shared.domain.repository

import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for device-specific preference operations.
 *
 * All methods may throw standard exceptions which will be handled by use cases
 * or ViewModels using the .asResult() extension.
 *
 * Follows NIA pattern - uses standard exceptions, no custom exception hierarchies.
 */
interface DevicePreferencesRepository {

    /**
     * Observes the current theme preference.
     *
     * @return Flow that emits [com.qodein.shared.model.Theme] preference, defaults to [com.qodein.shared.model.Theme.SYSTEM]
     */
    fun getTheme(): Flow<Theme>

    /**
     * Updates the theme preference.
     *
     * @param theme The theme to set
     * @throws java.io.IOException when storage operation fails
     */
    suspend fun setTheme(theme: Theme)

    /**
     * Observes the current language preference.
     *
     * @return Flow that emits [com.qodein.shared.model.Language] preference, defaults to [com.qodein.shared.model.Language.ENGLISH]
     */
    fun getLanguage(): Flow<Language>

    /**
     * Updates the language preference.
     *
     * @param language The language to set
     * @throws java.io.IOException when storage operation fails
     */
    suspend fun setLanguage(language: Language)

    /**
     * Clears all device preferences.
     *
     * @throws java.io.IOException when storage operation fails
     */
    suspend fun clearPreferences()
}
