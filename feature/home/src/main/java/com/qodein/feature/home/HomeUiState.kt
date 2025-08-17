package com.qodein.feature.home

import com.qodein.core.model.Banner
import com.qodein.core.model.PromoCode

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Refreshing(val previousData: Success? = null) : HomeUiState

    data class Success(
        val banners: List<Banner> = emptyList(),
        val promoCodes: List<PromoCode>,
        val hasMorePromoCodes: Boolean,
        val isLoadingMore: Boolean = false
    ) : HomeUiState

    data class Error(val exception: Throwable, val isRetryable: Boolean = true) : HomeUiState
}
