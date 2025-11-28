package com.qodein.core.data.repository

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.qodein.core.data.datasource.DevicePreferencesDataSource
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import java.util.Locale

class DevicePreferencesRepositoryImpl(private val context: Context, private val dataSource: DevicePreferencesDataSource) :
    DevicePreferencesRepository {

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

    override fun getLanguage(): Flow<Language> {
        // On Android 13+, observe system app-specific locale changes
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            callbackFlow {
                // Emit initial language
                Timber.d("callbackFlow: Emitting initial language")
                trySend(getSystemLanguage())

                // Register configuration change listener
                val configListener = object : ComponentCallbacks {
                    override fun onConfigurationChanged(newConfig: Configuration) {
                        Timber.d("Configuration changed, emitting new language")
                        trySend(getSystemLanguage())
                    }

                    override fun onLowMemory() {}
                }

                context.registerComponentCallbacks(configListener)
                Timber.d("Configuration listener registered")

                // Cleanup when flow is cancelled
                awaitClose {
                    Timber.d("Unregistering configuration listener")
                    context.unregisterComponentCallbacks(configListener)
                }
            }
        } else {
            // On Android <13, use DataStore preference
            dataSource.getLanguage().map { languageCode ->
                languageCode?.let {
                    Language.entries.find { lang -> lang.code == it }
                } ?: getSystemLanguage()
            }
        }
    }

    private fun getSystemLanguage(): Language {
        val systemLocale = Locale.getDefault().language
        Timber.d("getSystemLanguage() called, locale: %s", systemLocale)
        return when (systemLocale) {
            "en" -> Language.ENGLISH
            "kk" -> Language.KAZAKH
            "ru" -> Language.RUSSIAN
            else -> Language.ENGLISH
        }
    }

    override suspend fun setTheme(theme: Theme): Result<Unit, OperationError> =
        try {
            Timber.d("Setting theme: %s", theme)
            dataSource.setTheme(theme.name)
            Timber.i("Theme set successfully: %s", theme)
            Result.Success(Unit)
        } catch (e: IOException) {
            Timber.e(e, "I/O error setting theme: %s", e.message)
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error setting theme: %s - %s", e::class.simpleName, e.message)
            Result.Error(SystemError.Unknown)
        }

    override suspend fun setLanguage(language: Language): Result<Unit, OperationError> =
        try {
            Timber.d("Setting language: %s", language)
            dataSource.setLanguage(language.code)
            Timber.i("Language set successfully: %s", language)
            Result.Success(Unit)
        } catch (e: IOException) {
            Timber.e(e, "I/O error setting language: %s", e.message)
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error setting language: %s - %s", e::class.simpleName, e.message)
            Result.Error(SystemError.Unknown)
        }
}
