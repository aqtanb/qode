package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for device-specific preference operations.
 * Returns Result<D, OperationError> for type-safe error handling.
 * Repository implementations handle exception translation to domain errors.
 */
interface DevicePreferencesRepository {

    /**
     * Observes the current theme preference.
     * Returns Flow without Result wrapper since preferences rarely fail.
     */
    fun getTheme(): Flow<Theme>

    /**
     * Updates the theme preference.
     */
    suspend fun setTheme(theme: Theme): Result<Unit, OperationError>

    /**
     * Observes the current language preference.
     * Returns Flow without Result wrapper since preferences rarely fail.
     */
    fun getLanguage(): Flow<Language>

    /**
     * Updates the language preference.
     */
    suspend fun setLanguage(language: Language): Result<Unit, OperationError>
}
