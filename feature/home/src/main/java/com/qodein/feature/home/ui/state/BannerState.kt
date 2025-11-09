package com.qodein.feature.home.ui.state

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Banner
import com.qodein.shared.model.Language

sealed interface BannerState {
    data class Success(val banners: List<Banner>, val userLanguage: Language) : BannerState
    data object Loading : BannerState
    data class Error(val error: OperationError) : BannerState
}
