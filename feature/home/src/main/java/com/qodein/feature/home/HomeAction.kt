package com.qodein.feature.home

import com.qodein.core.model.PromoCode
import com.qodein.core.ui.component.HeroBannerItem

sealed interface HomeAction {

    data object RefreshData : HomeAction

    data class BannerItemClicked(val item: HeroBannerItem) : HomeAction

    data class PromoCodeClicked(val promoCode: PromoCode) : HomeAction

    data class UpvotePromoCode(val promoCodeId: String) : HomeAction

    data class FollowStore(val storeId: String) : HomeAction

    data class CopyPromoCode(val promoCode: PromoCode) : HomeAction

    data object LoadMorePromoCodes : HomeAction

    data object ErrorDismissed : HomeAction
}
