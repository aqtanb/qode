package com.qodein.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CacheMetadataDataSource(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val POPULAR_SERVICES_LAST_FETCH = longPreferencesKey("popular_services_last_fetch")
    }

    suspend fun getPopularServicesLastFetch(): Long? =
        dataStore.data.map { preferences ->
            preferences[POPULAR_SERVICES_LAST_FETCH]
        }.first()

    suspend fun setPopularServicesLastFetch(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[POPULAR_SERVICES_LAST_FETCH] = timestamp
        }
    }
}
