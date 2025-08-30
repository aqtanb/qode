package com.qodein.feature.home

import com.qodein.feature.home.ui.state.BannerState
import com.qodein.feature.home.ui.state.PromoCodeState
import com.qodein.feature.home.ui.state.ServiceSearchState
import com.qodein.shared.model.CompleteFilterState
import com.qodein.shared.ui.FilterDialogType

/**
 * Single HomeUiState following MVI pattern
 * Contains independent states for different content types
 */
data class HomeUiState(
    // Content states (independent loading/error/success)
    val bannerState: BannerState = BannerState.Loading,
    val promoCodeState: PromoCodeState = PromoCodeState.Loading,

    // Filter management
    val currentFilters: CompleteFilterState = CompleteFilterState(),
    val activeFilterDialog: FilterDialogType? = null,

    // Service search (for service filter dialog)
    val serviceSearchState: ServiceSearchState = ServiceSearchState.Empty,

    // Pagination state
    val isLoadingMore: Boolean = false
) {

    // Convenience properties for UI
    val isInitialLoading: Boolean
        get() = bannerState is BannerState.Loading && promoCodeState is PromoCodeState.Loading

    val hasContent: Boolean
        get() = bannerState is BannerState.Success || promoCodeState is PromoCodeState.Success

    val hasActiveFilters: Boolean
        get() = currentFilters.hasActiveFilters

    val isDialogVisible: Boolean
        get() = activeFilterDialog != null
}
