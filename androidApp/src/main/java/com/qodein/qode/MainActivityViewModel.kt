package com.qodein.qode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(devicePreferencesRepository: DevicePreferencesRepository) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = combine(
        devicePreferencesRepository.getTheme(),
        devicePreferencesRepository.getLanguage(),
    ) { theme, language ->
        MainActivityUiState.Success(theme, language)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState

    data class Success(val theme: Theme, val language: Language) : MainActivityUiState {
        override fun shouldUseDarkTheme(isSystemDarkTheme: Boolean): Boolean =
            when (theme) {
                Theme.SYSTEM -> isSystemDarkTheme
                Theme.LIGHT -> false
                Theme.DARK -> true
            }
    }

    /**
     * Returns `true` if the state wasn't loaded yet and it should keep showing the splash screen.
     */
    fun shouldKeepSplashScreen() = this is Loading

    /**
     * Returns `true` if dark theme should be used.
     */
    fun shouldUseDarkTheme(isSystemDarkTheme: Boolean) = isSystemDarkTheme
}
