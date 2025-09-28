package com.qodein.core.data.repository

import com.qodein.core.data.datasource.DevicePreferencesDataSource
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicePreferencesRepositoryImpl @Inject constructor(private val dataSource: DevicePreferencesDataSource) :
    DevicePreferencesRepository {

    // Getters don't fail, so no Result wrapper needed
    override fun getTheme(): Flow<Theme> = dataSource.getTheme()

    override fun getLanguage(): Flow<Language> = dataSource.getLanguage()

    // Setters can fail, so wrap in Result
    override suspend fun setTheme(theme: Theme): Result<Unit, OperationError> =
        try {
            dataSource.setTheme(theme)
            Result.Success(Unit)
        } catch (e: IOException) {
            Result.Error(SystemError.Unknown)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }

    override suspend fun setLanguage(language: Language): Result<Unit, OperationError> =
        try {
            dataSource.setLanguage(language)
            Result.Success(Unit)
        } catch (e: IOException) {
            Result.Error(SystemError.Unknown)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }

    override suspend fun clearPreferences(): Result<Unit, OperationError> =
        try {
            dataSource.clear()
            Result.Success(Unit)
        } catch (e: IOException) {
            Result.Error(SystemError.Unknown)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }
}
