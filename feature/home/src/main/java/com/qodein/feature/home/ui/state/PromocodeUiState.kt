package com.qodein.feature.home.ui.state

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.PromoCode

/**
 * Independent promo code state for home screen
 */
sealed class PromocodeUiState {
    data object Loading : PromocodeUiState()
    data class Success(
        val promoCodes: List<PromoCode>,
        val hasMore: Boolean = false,
        val nextCursor: PaginationCursor<ContentSortBy>? = null
    ) : PromocodeUiState()
    data object Empty : PromocodeUiState()
    data class Error(val error: OperationError) : PromocodeUiState()
}
