package com.qodein.feature.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.shared.common.Result
import com.qodein.shared.domain.usecase.preferences.GetThemeUseCase
import com.qodein.shared.domain.usecase.preferences.ObserveLanguageUseCase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        observePreferences()
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.ShowLanguageBottomSheet -> handleLanguageSettingClick()
            SettingsAction.HideLanguageBottomSheet -> _uiState.update { it.copy(showLanguageBottomSheet = false) }
            is SettingsAction.LanguageChanged -> setLanguage(action.language)

            SettingsAction.ShowThemeBottomSheet -> _uiState.update { it.copy(showThemeBottomSheet = true) }
            SettingsAction.HideThemeBottomSheet -> _uiState.update { it.copy(showThemeBottomSheet = false) }
            is SettingsAction.ThemeChanged -> setTheme(action.theme)

            SettingsAction.AboutAppClicked -> TODO()
            SettingsAction.FeedbackClicked -> TODO()
            SettingsAction.NotificationsClicked -> TODO()
            SettingsAction.OpenSourceLicencesClicked -> TODO()
            SettingsAction.RateAppClicked -> TODO()
            SettingsAction.SourceCodeClicked -> TODO()
        }
    }

    private fun observePreferences() {
        Logger.d("SettingsViewModel") { "observePreferences() started" }
        combine(
            getThemeUseCase(),
            observeLanguageUseCase(),
        ) { themeResult, languageResult ->
            Logger.d("SettingsViewModel") { "Received preferences - theme: $themeResult, language: $languageResult" }

            val theme = when (themeResult) {
                is Result.Success -> themeResult.data
                is Result.Error -> Theme.SYSTEM // Fallback to system theme
            }

            val language = when (languageResult) {
                is Result.Success -> {
                    Logger.d("SettingsViewModel") { "Language received: ${languageResult.data}" }
                    languageResult.data
                }
                is Result.Error -> {
                    Logger.e("SettingsViewModel") { "Language error: ${languageResult.error}" }
                    Language.ENGLISH // Fallback to English
                }
            }

            val error = when {
                themeResult is Result.Error -> themeResult.error
                languageResult is Result.Error -> languageResult.error
                else -> null
            }

            _uiState.value = _uiState.value.copy(
                theme = theme,
                language = language,
                error = error,
            )
        }.launchIn(viewModelScope)
    }

    private fun setTheme(theme: Theme) {
        val previousTheme = _uiState.value.theme

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            setThemeUseCase(theme).collect { result ->
                when (result) {
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
                        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = result.error)
                    }
                }
            }
        }
    }

    private fun handleLanguageSettingClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // On Android 13+, open system app-specific language settings
            emitEvent(SettingsEvent.OpenSystemLanguageSettings)
        } else {
            // On Android <13, show in-app language bottom sheet
            _uiState.update { it.copy(showLanguageBottomSheet = true) }
        }
    }

    private fun setLanguage(language: Language) {
        val previousLanguage = _uiState.value.language
        Logger.d("SettingsViewModel") { "setLanguage called: $previousLanguage -> $language" }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            setLanguageUseCase(language).collect { result ->
                Logger.d("SettingsViewModel") { "setLanguageUseCase result: $result" }
                when (result) {
                    is Result.Success -> {
                        Logger.d("SettingsViewModel") { "Language set successfully to: $language" }
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
                        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    }
                    is Result.Error -> {
                        Logger.e("SettingsViewModel") { "Failed to set language: ${result.error}" }
                        _uiState.value = _uiState.value.copy(isLoading = false, error = result.error)
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
}
