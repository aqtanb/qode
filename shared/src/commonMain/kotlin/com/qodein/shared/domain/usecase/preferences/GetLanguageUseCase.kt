package com.qodein.shared.domain.usecase.preferences

import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Use case for observing language preference.
 *
 * Wraps DevicePreferencesRepository with Result pattern for proper error handling.
 */

class GetLanguageUseCase constructor(private val devicePreferencesRepository: DevicePreferencesRepository) {

    /**
     * Get language preference as Flow<Result<Language>>
     *
     * @return Flow that emits Result.success(Language) or Result.failure(Throwable)
     */
    operator fun invoke(): Flow<Result<Language>> =
        devicePreferencesRepository.getLanguage()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
