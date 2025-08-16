package com.qodein.feature.settings

import com.qodein.core.model.Language
import com.qodein.core.model.Theme

data class SettingsUiState(
    val theme: Theme = Theme.SYSTEM,
    val language: Language = Language.ENGLISH,
    val isLoading: Boolean = false,
    val error: Throwable? = null
)
