package com.qodein.feature.home.ui.state

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.PromoCode

/**
 * Independent promo code state for home screen
 */
sealed class PromoCodeState {
    data object Loading : PromoCodeState()
    data class Success(val promoCodes: List<PromoCode>, val hasMore: Boolean = false, val nextCursor: PaginationCursor? = null) :
        PromoCodeState()
    data object Empty : PromoCodeState()
    data class Error(val errorType: ErrorType, val isRetryable: Boolean, val shouldShowSnackbar: Boolean, val errorCode: String?) :
        PromoCodeState()
}
