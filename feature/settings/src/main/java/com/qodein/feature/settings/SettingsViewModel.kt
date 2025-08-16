package com.qodein.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.domain.usecase.preferences.GetLanguageUseCase
import com.qodein.core.domain.usecase.preferences.GetThemeUseCase
import com.qodein.core.domain.usecase.preferences.SetLanguageUseCase
import com.qodein.core.domain.usecase.preferences.SetThemeUseCase
import com.qodein.core.model.Language
import com.qodein.core.model.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val setLanguageUseCase: SetLanguageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        observePreferences()
    }

    fun handleAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.ThemeChanged -> setTheme(action.theme)
            is SettingsAction.LanguageChanged -> setLanguage(action.language)
        }
    }

    private fun observePreferences() {
        combine(
            getThemeUseCase(),
            getLanguageUseCase(),
        ) { themeResult, languageResult ->
            _state.value = _state.value.copy(
                theme = themeResult.getOrElse { Theme.SYSTEM },
                language = languageResult.getOrElse { Language.ENGLISH },
                error = themeResult.exceptionOrNull() ?: languageResult.exceptionOrNull(),
            )
        }.launchIn(viewModelScope)
    }

    private fun setTheme(theme: Theme) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                setThemeUseCase(theme)
                emitEvent(SettingsEvent.ThemeChanged)
            } catch (exception: Throwable) {
                _state.value = _state.value.copy(error = exception)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun setLanguage(language: Language) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                setLanguageUseCase(language)
                emitEvent(SettingsEvent.LanguageChanged)
            } catch (exception: Throwable) {
                _state.value = _state.value.copy(error = exception)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun emitEvent(event: SettingsEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
