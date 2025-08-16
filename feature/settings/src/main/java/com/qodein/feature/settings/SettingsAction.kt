package com.qodein.feature.settings

import com.qodein.core.model.Language
import com.qodein.core.model.Theme

sealed interface SettingsAction {
    data class ThemeChanged(val theme: Theme) : SettingsAction
    data class LanguageChanged(val language: Language) : SettingsAction
}
