package com.qodein.shared.domain.usecase.preferences

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Use case for observing theme preference.
 *
 * Wraps DevicePreferencesRepository with Result pattern for proper error handling.
 */

class GetThemeUseCase(private val devicePreferencesRepository: DevicePreferencesRepository) {

    operator fun invoke(): Flow<Result<Theme, OperationError>> =
        devicePreferencesRepository.getTheme()
            .map<Theme, Result<Theme, OperationError>> { theme -> Result.Success(theme) }
            .catch { emit(Result.Error(SystemError.Unknown)) }
}
