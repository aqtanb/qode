package com.qodein.feature.home

import com.qodein.core.model.PromoCode

sealed interface HomeEvent {

    data class PromoCodeDetailRequested(val promoCode: PromoCode) : HomeEvent

    data object BannerDetailRequested : HomeEvent

    data class PromoCodeCopied(val promoCode: PromoCode) : HomeEvent
}
