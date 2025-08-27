package com.qodein.feature.home

import com.qodein.feature.home.model.CategoryFilter
import com.qodein.feature.home.model.FilterDialogType
import com.qodein.feature.home.model.ServiceFilter
import com.qodein.feature.home.model.SortFilter
import com.qodein.shared.model.Banner
import com.qodein.shared.model.PromoCode

sealed interface HomeAction {

    data object RefreshData : HomeAction

    data class BannerClicked(val banner: Banner) : HomeAction

    data class PromoCodeClicked(val promoCode: PromoCode) : HomeAction

    data class UpvotePromoCode(val promoCodeId: String) : HomeAction

    data class DownvotePromoCode(val promoCodeId: String) : HomeAction

    data class CopyPromoCode(val promoCode: PromoCode) : HomeAction

    data object LoadMorePromoCodes : HomeAction

    data object RetryClicked : HomeAction

    data object ErrorDismissed : HomeAction

    // Filter Actions
    data class ShowFilterDialog(val type: FilterDialogType) : HomeAction

    data object DismissFilterDialog : HomeAction

    data class ApplyCategoryFilter(val categoryFilter: CategoryFilter) : HomeAction

    data class ApplyServiceFilter(val serviceFilter: ServiceFilter) : HomeAction

    data class ApplySortFilter(val sortFilter: SortFilter) : HomeAction

    data object ResetFilters : HomeAction

    // Service search actions
    data class SearchServices(val query: String) : HomeAction
}
