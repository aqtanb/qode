package com.qodein.feature.settings

import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme

data class SettingsUiState(
    val theme: Theme = Theme.SYSTEM,
    val language: Language = Language.ENGLISH,
    val isLoading: Boolean = false,
    val error: Throwable? = null
)
