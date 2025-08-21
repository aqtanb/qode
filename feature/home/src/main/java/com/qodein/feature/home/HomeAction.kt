package com.qodein.feature.home

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
}
