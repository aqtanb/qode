package com.qodein.feature.settings

import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme

sealed interface SettingsAction {
    data class ThemeChanged(val theme: Theme) : SettingsAction
    data class LanguageChanged(val language: Language) : SettingsAction
}
