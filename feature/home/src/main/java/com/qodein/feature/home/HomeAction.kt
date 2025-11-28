package com.qodein.feature.home

import com.qodein.shared.model.Banner
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.ServiceFilter
import com.qodein.shared.model.SortFilter
import com.qodein.shared.ui.FilterDialogType

sealed interface HomeAction {

    data object RefreshData : HomeAction

    data class BannerClicked(val banner: Banner) : HomeAction

    data class PromoCodeClicked(val promocodeId: PromocodeId) : HomeAction
    data class CopyPromoCode(val promoCode: Promocode) : HomeAction

    data object LoadMorePromoCodes : HomeAction

    data object RetryBannersClicked : HomeAction
    data object RetryPromoCodesClicked : HomeAction

    data object ErrorDismissed : HomeAction

    // Filter Actions
    data class ShowFilterDialog(val type: FilterDialogType) : HomeAction

    data object DismissFilterDialog : HomeAction

    data class ApplyServiceFilter(val serviceFilter: ServiceFilter) : HomeAction

    data class ApplySortFilter(val sortFilter: SortFilter) : HomeAction

    data object ResetFilters : HomeAction
}
