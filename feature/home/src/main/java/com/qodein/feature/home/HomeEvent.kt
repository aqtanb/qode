package com.qodein.feature.home

import com.qodein.shared.model.Banner
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromocodeId

sealed interface HomeEvent {

    data class PromoCodeDetailRequested(val promocodeId: PromocodeId) : HomeEvent

    data class BannerDetailRequested(val banner: Banner) : HomeEvent

    data class PromoCodeCopied(val promoCode: PromoCode) : HomeEvent
}
