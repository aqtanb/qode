package com.qodein.feature.home

import com.qodein.core.model.PromoCode
import com.qodein.core.ui.component.HeroBannerItem

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data object Refreshing : HomeUiState

    data class Success(
        val promoCodes: List<PromoCode>,
        val bannerItems: List<HeroBannerItem>,
        val hasMorePromoCodes: Boolean,
        val isLoadingMore: Boolean = false
    ) : HomeUiState {

        val isEmpty: Boolean
            get() = promoCodes.isEmpty() && bannerItems.isEmpty()

        val hasContent: Boolean
            get() = promoCodes.isNotEmpty() || bannerItems.isNotEmpty()
    }

    data class Error(val exception: Throwable, val isRetryable: Boolean = true) : HomeUiState
}
