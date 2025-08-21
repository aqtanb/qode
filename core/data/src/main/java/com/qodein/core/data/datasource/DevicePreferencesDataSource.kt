package com.qodein.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicePreferencesDataSource @Inject constructor(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    /**
     * Observes theme preference
     */
    fun getTheme(): Flow<Theme> =
        dataStore.data.map { preferences ->
            val themeName = preferences[THEME_KEY] ?: Theme.SYSTEM.name
            try {
                Theme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                // Log malformed theme preference but don't throw
                Theme.SYSTEM
            } catch (e: Exception) {
                throw IllegalStateException("service unavailable: failed to read theme preference", e)
            }
        }

    /**
     * Updates theme preference
     */
    suspend fun setTheme(theme: Theme) {
        try {
            dataStore.edit { preferences ->
                preferences[THEME_KEY] = theme.name
            }
        } catch (e: Exception) {
            throw IllegalStateException("service unavailable: failed to save theme preference", e)
        }
    }

    /**
     * Observes language preference
     */
    fun getLanguage(): Flow<Language> =
        dataStore.data.map { preferences ->
            val languageCode = preferences[LANGUAGE_KEY] ?: Language.RUSSIAN.code // Default to Russian for KZ market
            Language.entries.find { it.code == languageCode } ?: Language.RUSSIAN
        }

    /**
     * Updates language preference
     */
    suspend fun setLanguage(language: Language) {
        try {
            dataStore.edit { preferences ->
                preferences[LANGUAGE_KEY] = language.code
            }
        } catch (e: Exception) {
            throw IllegalStateException("service unavailable: failed to save language preference", e)
        }
    }

    /**
     * Clears all preferences
     */
    suspend fun clear() {
        try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        } catch (e: Exception) {
            throw IllegalStateException("service unavailable: failed to clear preferences", e)
        }
    }
}
