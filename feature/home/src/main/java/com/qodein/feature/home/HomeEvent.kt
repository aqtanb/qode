package com.qodein.feature.home

import com.qodein.core.model.PromoCode
import com.qodein.core.ui.component.HeroBannerItem

sealed interface HomeEvent {

    data class PromoCodeDetailRequested(val promoCode: PromoCode) : HomeEvent

    data class BannerDetailRequested(val item: HeroBannerItem) : HomeEvent

    data class PromoCodeCopied(val promoCode: PromoCode) : HomeEvent
}
