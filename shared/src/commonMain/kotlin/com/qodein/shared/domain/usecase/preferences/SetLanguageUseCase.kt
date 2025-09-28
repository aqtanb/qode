package com.qodein.shared.domain.usecase.preferences

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for updating language preference.
 *
 * Handles language preference updates with proper error propagation.
 */

class SetLanguageUseCase(private val devicePreferencesRepository: DevicePreferencesRepository) {

    operator fun invoke(language: Language): Flow<Result<Unit, OperationError>> =
        flow {
            val result = devicePreferencesRepository.setLanguage(language)
            emit(result)
        }
}
