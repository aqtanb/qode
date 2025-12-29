package com.qodein.feature.settings

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme

data class SettingsUiState(
    val theme: Theme = Theme.SYSTEM,
    val language: Language = Language.ENGLISH,
    val showLanguageBottomSheet: Boolean = false,
    val showThemeBottomSheet: Boolean = false,
    val isLoading: Boolean = false,
    val error: OperationError? = null,
    val showDeleteAccountDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteAccountError: OperationError? = null
)
