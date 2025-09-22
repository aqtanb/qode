package com.qodein.shared.domain.usecase.preferences

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Theme
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing theme preference.
 *
 * Wraps DevicePreferencesRepository with Result pattern for proper error handling.
 */

class GetThemeUseCase(private val devicePreferencesRepository: DevicePreferencesRepository) {

    /**
     * Get theme preference as Flow<Result<Theme>>
     *
     * @return Flow that emits Result.Success(Theme), Result.Loading, or Result.Error(Throwable)
     */
    operator fun invoke(): Flow<Result<Theme>> = devicePreferencesRepository.getTheme().asResult()
}
