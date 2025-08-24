package com.qodein.feature.home

import com.qodein.feature.home.model.FilterDialogType
import com.qodein.feature.home.model.FilterState
import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.Banner
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.Service

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Refreshing(val previousData: Success? = null) : HomeUiState

    data class Success(
        val banners: List<Banner> = emptyList(),
        val promoCodes: List<PromoCode>,
        val hasMorePromoCodes: Boolean,
        val isLoadingMore: Boolean = false,
        val availableServices: List<Service> = emptyList(),
        val currentFilters: FilterState = FilterState(),
        val activeFilterDialog: FilterDialogType? = null
    ) : HomeUiState

    data class Error(
        val errorType: ErrorType,
        val isRetryable: Boolean,
        val shouldShowSnackbar: Boolean = true,
        val errorCode: String? = null
    ) : HomeUiState
}
