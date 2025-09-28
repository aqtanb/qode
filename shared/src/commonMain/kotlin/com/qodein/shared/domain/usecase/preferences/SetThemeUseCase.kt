package com.qodein.shared.domain.usecase.preferences

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for updating theme preference.
 *
 * Handles theme preference updates with proper error propagation.
 */

class SetThemeUseCase(private val devicePreferencesRepository: DevicePreferencesRepository) {

    operator fun invoke(theme: Theme): Flow<Result<Unit, OperationError>> =
        flow {
            val result = devicePreferencesRepository.setTheme(theme)
            emit(result)
        }
}
