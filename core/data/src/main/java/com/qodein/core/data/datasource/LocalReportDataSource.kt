package com.qodein.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.qodein.shared.model.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalReportDataSource(private val reportDataStore: DataStore<Preferences>) {
    companion object {
        private val REPORTED_CONTENT_IDS = stringSetPreferencesKey("reported_content_ids")
        private const val SEPARATOR = ":"
    }

    val reportedContentIds: Flow<Set<String>> = reportDataStore.data.map { preferences ->
        preferences[REPORTED_CONTENT_IDS] ?: emptySet()
    }

    suspend fun addReportedContentId(
        contentId: String,
        type: ContentType
    ) {
        val compositeKey = "${type.name}$SEPARATOR$contentId"
        reportDataStore.edit { preferences ->
            val currentIds = preferences[REPORTED_CONTENT_IDS].orEmpty()
            preferences[REPORTED_CONTENT_IDS] = currentIds + compositeKey
        }
    }
}
