package com.qodein.feature.home.ui.state

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.Banner

/**
 * Independent banner state for home screen
 */
sealed class BannerState {
    data class Success(val banners: List<Banner>) : BannerState()
    data object Loading : BannerState()
    data class Error(val errorType: ErrorType, val isRetryable: Boolean, val shouldShowSnackbar: Boolean, val errorCode: String?) :
        BannerState()
}
