package com.qodein.feature.home.ui.state

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Banner

/**
 * Independent banner state for home screen
 */
sealed class BannerState {
    data class Success(val banners: List<Banner>) : BannerState()
    data object Loading : BannerState()
    data class Error(val errorType: OperationError, val isRetryable: Boolean, val shouldShowSnackbar: Boolean, val errorCode: String?) :
        BannerState()
}
