package com.qodein.core.data.repository

import com.qodein.core.data.datasource.DevicePreferencesDataSource
import com.qodein.core.domain.repository.DevicePreferencesRepository
import com.qodein.core.model.Language
import com.qodein.core.model.Theme
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicePreferencesRepositoryImpl @Inject constructor(private val dataSource: DevicePreferencesDataSource) :
    DevicePreferencesRepository {

    override fun getTheme(): Flow<Theme> = dataSource.getTheme()

    override suspend fun setTheme(theme: Theme) = dataSource.setTheme(theme)

    override fun getLanguage(): Flow<Language> = dataSource.getLanguage()

    override suspend fun setLanguage(language: Language) = dataSource.setLanguage(language)

    override suspend fun clearPreferences() = dataSource.clear()
}
