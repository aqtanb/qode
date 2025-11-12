package com.qodein.feature.home

import com.qodein.feature.home.ui.state.BannerState
import com.qodein.feature.home.ui.state.PromoCodeState
import com.qodein.shared.domain.service.selection.SelectionState
import com.qodein.shared.domain.service.selection.ServiceSelectionState
import com.qodein.shared.model.CompleteFilterState
import com.qodein.shared.model.Language
import com.qodein.shared.ui.FilterDialogType

/**
 * Single HomeUiState following MVI pattern
 * Contains independent states for different content types
 */
data class HomeUiState(
    val bannerState: BannerState = BannerState.Loading,
    val promoCodeState: PromoCodeState = PromoCodeState.Loading,

    val userLanguage: Language = Language.ENGLISH,

    val currentFilters: CompleteFilterState = CompleteFilterState(),
    val activeFilterDialog: FilterDialogType? = null,
    val serviceSelectionState: ServiceSelectionState = ServiceSelectionState(
        selection = SelectionState.Multi(),
    ),
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false
)
