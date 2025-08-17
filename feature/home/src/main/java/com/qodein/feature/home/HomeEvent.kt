package com.qodein.feature.home

import com.qodein.core.model.Banner
import com.qodein.core.model.PromoCode

sealed interface HomeEvent {

    data class PromoCodeDetailRequested(val promoCode: PromoCode) : HomeEvent

    data class BannerDetailRequested(val banner: Banner) : HomeEvent

    data class PromoCodeCopied(val promoCode: PromoCode) : HomeEvent
}
