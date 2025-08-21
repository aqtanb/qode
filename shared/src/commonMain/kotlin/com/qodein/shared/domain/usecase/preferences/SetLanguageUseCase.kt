package com.qodein.shared.domain.usecase.preferences

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for updating language preference.
 *
 * Handles language preference updates with proper error propagation.
 */

class SetLanguageUseCase constructor(private val devicePreferencesRepository: DevicePreferencesRepository) {

    /**
     * Set language preference
     *
     * @param language The language to set
     * @return Flow<Result<Unit>> that emits success or error
     */
    operator fun invoke(language: Language): Flow<Result<Unit>> =
        flow {
            emit(Unit)
            devicePreferencesRepository.setLanguage(language)
        }.asResult()
}
