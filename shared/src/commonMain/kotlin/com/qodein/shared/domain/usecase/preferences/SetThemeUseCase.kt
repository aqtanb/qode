package com.qodein.shared.domain.usecase.preferences

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for updating theme preference.
 *
 * Handles theme preference updates with proper error propagation.
 */

class SetThemeUseCase constructor(private val devicePreferencesRepository: DevicePreferencesRepository) {

    /**
     * Set theme preference
     *
     * @param theme The theme to set
     * @return Flow<Result<Unit>> that emits success or error
     */
    operator fun invoke(theme: Theme): Flow<Result<Unit>> =
        flow {
            emit(Unit)
            devicePreferencesRepository.setTheme(theme)
        }.asResult()
}
