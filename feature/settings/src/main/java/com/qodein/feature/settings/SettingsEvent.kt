package com.qodein.feature.settings

sealed interface SettingsEvent {
    data object NavigateBack : SettingsEvent
    data object OpenSystemLanguageSettings : SettingsEvent
    data object AccountDeleted : SettingsEvent
}
