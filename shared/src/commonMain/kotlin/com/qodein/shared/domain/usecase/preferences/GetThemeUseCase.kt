package com.qodein.shared.domain.usecase.preferences

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

class GetThemeUseCase constructor(private val devicePreferencesRepository: DevicePreferencesRepository) {

    /**
     * Get theme preference as Flow<Result<Theme>>
     *
     * @return Flow that emits Result.success(Theme) or Result.failure(Throwable)
     */
    operator fun invoke(): Flow<Result<Theme>> =
        devicePreferencesRepository.getTheme()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
