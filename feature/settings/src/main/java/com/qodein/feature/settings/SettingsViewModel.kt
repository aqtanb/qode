package com.qodein.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.shared.common.Result
import com.qodein.shared.domain.usecase.preferences.GetLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.GetThemeUseCase
import com.qodein.shared.domain.usecase.preferences.SetLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.SetThemeUseCase
import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme
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
    private val setLanguageUseCase: SetLanguageUseCase,
    private val analyticsHelper: AnalyticsHelper
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
            val theme = when (themeResult) {
                is Result.Loading -> _state.value.theme // Keep current theme while loading
                is Result.Success -> themeResult.data
                is Result.Error -> Theme.SYSTEM // Fallback to system theme
            }

            val language = when (languageResult) {
                is Result.Loading -> _state.value.language // Keep current language while loading
                is Result.Success -> languageResult.data
                is Result.Error -> Language.ENGLISH // Fallback to English
            }

            val error = when {
                themeResult is Result.Error -> themeResult.exception
                languageResult is Result.Error -> languageResult.exception
                else -> null
            }

            _state.value = _state.value.copy(
                theme = theme,
                language = language,
                error = error,
            )
        }.launchIn(viewModelScope)
    }

    private fun setTheme(theme: Theme) {
        val previousTheme = _state.value.theme

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            setThemeUseCase(theme).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Result.Success -> {
                        // Track theme change
                        analyticsHelper.logEvent(
                            AnalyticsEvent(
                                type = "theme_changed",
                                extras = listOf(
                                    AnalyticsEvent.Param("theme_from", previousTheme.name.lowercase()),
                                    AnalyticsEvent.Param("theme_to", theme.name.lowercase()),
                                ),
                            ),
                        )
                        _state.value = _state.value.copy(isLoading = false, error = null)
                        emitEvent(SettingsEvent.ThemeChanged)
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.exception)
                    }
                }
            }
        }
    }

    private fun setLanguage(language: Language) {
        val previousLanguage = _state.value.language

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            setLanguageUseCase(language).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Result.Success -> {
                        // Track language change
                        analyticsHelper.logEvent(
                            AnalyticsEvent(
                                type = "language_changed",
                                extras = listOf(
                                    AnalyticsEvent.Param("language_from", previousLanguage.code),
                                    AnalyticsEvent.Param("language_to", language.code),
                                ),
                            ),
                        )
                        _state.value = _state.value.copy(isLoading = false, error = null)
                        emitEvent(SettingsEvent.LanguageChanged)
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.exception)
                    }
                }
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
