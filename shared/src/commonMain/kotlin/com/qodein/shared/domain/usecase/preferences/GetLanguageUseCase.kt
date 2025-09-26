package com.qodein.shared.domain.usecase.preferences

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
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

class GetLanguageUseCase(private val devicePreferencesRepository: DevicePreferencesRepository) {

    operator fun invoke(): Flow<Result<Language, OperationError>> =
        devicePreferencesRepository.getLanguage()
            .map<Language, Result<Language, OperationError>> { language -> Result.Success(language) }
            .catch { emit(Result.Error(SystemError.Unknown)) }
}
