package com.qodein.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DevicePreferencesDataSource(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    fun getTheme(): Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[THEME_KEY]
        }

    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    fun getLanguage(): Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY]
        }

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
}
