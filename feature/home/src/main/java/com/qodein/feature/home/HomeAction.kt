package com.qodein.feature.home

import com.qodein.shared.model.Banner
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.PromocodeSortBy

sealed interface HomeAction {

    data object RefreshData : HomeAction

    data class BannerClicked(val banner: Banner) : HomeAction

    data class PromocodeClicked(val promocodeId: PromocodeId) : HomeAction
    data class CopyPromocode(val promocode: Promocode) : HomeAction

    data object LoadMorePromocodes : HomeAction

    data object RetryBannersClicked : HomeAction
    data object RetryPromocodesClicked : HomeAction

    data object ShowServiceSelection : HomeAction
    data class ApplySortFilter(val sortBy: PromocodeSortBy) : HomeAction
    data object ResetFilters : HomeAction
}
