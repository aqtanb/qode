package com.qodein.feature.settings

sealed interface SettingsEvent {
    data object NavigateBack : SettingsEvent
}
