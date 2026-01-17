package com.qodein.feature.home

import com.qodein.feature.home.BannerState
import com.qodein.feature.home.PromocodeUiState
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Banner
import com.qodein.shared.model.CompleteFilterState
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.Language
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.Promocode
import com.qodein.shared.ui.FilterDialogType

/**
 * Single HomeUiState following MVI pattern
 * Contains independent states for different content types
 */
data class HomeUiState(
    val bannerState: BannerState = BannerState.Loading,
    val promocodeUiState: PromocodeUiState = PromocodeUiState.Loading,

    val userLanguage: Language = Language.ENGLISH,

    val currentFilters: CompleteFilterState = CompleteFilterState(),
    val activeFilterDialog: FilterDialogType? = null,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false
)

sealed class PromocodeUiState {
    data object Loading : PromocodeUiState()
    data class Success(
        val promocodes: List<Promocode>,
        val hasMore: Boolean = false,
        val nextCursor: PaginationCursor<ContentSortBy>? = null
    ) : PromocodeUiState()
    data object Empty : PromocodeUiState()
    data class Error(val error: OperationError) : PromocodeUiState()
}

sealed interface BannerState {
    data class Success(val banners: List<Banner>) : BannerState
    data object Loading : BannerState
    data class Error(val error: OperationError) : BannerState
}
