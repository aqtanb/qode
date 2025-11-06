package com.qodein.core.data.repository

import com.qodein.core.data.datasource.DevicePreferencesDataSource
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class DevicePreferencesRepositoryImpl(private val dataSource: DevicePreferencesDataSource) : DevicePreferencesRepository {

    override fun getTheme(): Flow<Theme> =
        dataSource.getTheme().map { themeString ->
            themeString?.let {
                try {
                    Theme.valueOf(it)
                } catch (_: Exception) {
                    Theme.SYSTEM
                }
            } ?: Theme.SYSTEM
        }

    override fun getLanguage(): Flow<Language> =
        dataSource.getLanguage().map { languageCode ->
            languageCode?.let {
                Language.entries.find { lang -> lang.code == it }
            } ?: getSystemLanguage()
        }

    private fun getSystemLanguage(): Language {
        val systemLocale = Locale.getDefault().language
        return when (systemLocale) {
            "en" -> Language.ENGLISH
            "kk" -> Language.KAZAKH
            "ru" -> Language.RUSSIAN
            else -> Language.ENGLISH
        }
    }

    override suspend fun setTheme(theme: Theme): Result<Unit, OperationError> =
        try {
            dataSource.setTheme(theme.name)
            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Error(SystemError.Unknown)
        }

    override suspend fun setLanguage(language: Language): Result<Unit, OperationError> =
        try {
            dataSource.setLanguage(language.code)
            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Error(SystemError.Unknown)
        }
}
