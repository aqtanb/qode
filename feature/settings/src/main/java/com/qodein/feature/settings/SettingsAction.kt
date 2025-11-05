package com.qodein.feature.settings

import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme

sealed interface SettingsAction {
    data object ShowLanguageBottomSheet : SettingsAction
    data object HideLanguageBottomSheet : SettingsAction
    data class LanguageChanged(val language: Language) : SettingsAction

    data class ThemeChanged(val theme: Theme) : SettingsAction
    data object NotificationsClicked : SettingsAction
    data object SourceCodeClicked : SettingsAction
    data object OpenSourceLicencesClicked : SettingsAction
    data object AboutAppClicked : SettingsAction
    data object FeedbackClicked : SettingsAction
    data object RateAppClicked : SettingsAction
}
