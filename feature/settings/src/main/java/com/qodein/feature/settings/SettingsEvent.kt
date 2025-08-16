package com.qodein.feature.settings

sealed interface SettingsEvent {
    data object ThemeChanged : SettingsEvent
    data object LanguageChanged : SettingsEvent
}
