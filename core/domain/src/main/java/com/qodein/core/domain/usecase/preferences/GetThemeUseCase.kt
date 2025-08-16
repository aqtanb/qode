package com.qodein.core.domain.usecase.preferences

import com.qodein.core.domain.repository.DevicePreferencesRepository
import com.qodein.core.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for observing theme preference.
 *
 * Wraps DevicePreferencesRepository with Result pattern for proper error handling.
 */
@Singleton
class GetThemeUseCase @Inject constructor(private val devicePreferencesRepository: DevicePreferencesRepository) {

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
