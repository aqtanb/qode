package com.qodein.shared.domain.usecase.preferences

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Language
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing language preference.
 *
 * Wraps DevicePreferencesRepository with Result pattern for proper error handling.
 */

class GetLanguageUseCase constructor(private val devicePreferencesRepository: DevicePreferencesRepository) {

    /**
     * Get language preference as Flow<Result<Language>>
     *
     * @return Flow that emits Result.Success(Language), Result.Loading, or Result.Error(Throwable)
     */
    operator fun invoke(): Flow<Result<Language>> = devicePreferencesRepository.getLanguage().asResult()
}
