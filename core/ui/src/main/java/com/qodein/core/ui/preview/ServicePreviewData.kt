package com.qodein.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId

class ServicePreviewParameterProvider : PreviewParameterProvider<Service> {
    override val values: Sequence<Service>
        get() = sequenceOf(
            Service.fromDto(
                id = ServiceId("netflix-id"),
                name = "Netflix",
                siteUrl = "netflix.com",
                logoUrl = "https://img.logo.dev/netflix.com?token=pk_X-dlktBsQHWLGoIal-W1kA",
                promoCodeCount = 42,
            ),
            Service.fromDto(
                id = ServiceId("kaspi-id"),
                name = "Kaspi.kz",
                siteUrl = "kaspi.kz",
                logoUrl = "https://img.logo.dev/kaspi.kz?token=pk_X-dlktBsQHWLGoIal-W1kA",
                promoCodeCount = 67,
            ),
            Service.fromDto(
                id = ServiceId("spotify-id"),
                name = "Spotify",
                siteUrl = "spotify.com",
                logoUrl = "https://img.logo.dev/spotify.com?token=pk_X-dlktBsQHWLGoIal-W1kA",
                promoCodeCount = 28,
            ),
            Service.fromDto(
                id = ServiceId("long-name-id"),
                name = "Super Long Service Name Corporation Ltd",
                siteUrl = "custom.com",
                logoUrl = null,
                promoCodeCount = 0,
            ),
            Service.fromDto(
                id = ServiceId("zero-promo-id"),
                name = "Zero Promocodes",
                siteUrl = "example.com",
                logoUrl = null,
                promoCodeCount = 0,
            ),
        )
}

object ServicePreviewData {
    val netflix: Service = Service.fromDto(
        id = ServiceId("netflix-id"),
        name = "Netflix",
        siteUrl = "netflix.com",
        logoUrl = "https://img.logo.dev/netflix.com?token=pk_X-dlktBsQHWLGoIal-W1kA",
        promoCodeCount = 42,
    )

    val kaspi: Service = Service.fromDto(
        id = ServiceId("kaspi-id"),
        name = "Kaspi.kz",
        siteUrl = "kaspi.kz",
        logoUrl = "https://img.logo.dev/kaspi.kz?token=pk_X-dlktBsQHWLGoIal-W1kA",
        promoCodeCount = 67,
    )

    val glovo: Service = Service.fromDto(
        id = ServiceId("glovo-id"),
        name = "Glovo",
        siteUrl = "glovoapp.com",
        logoUrl = "https://img.logo.dev/glovoapp.com?token=pk_X-dlktBsQHWLGoIal-W1kA",
        promoCodeCount = 34,
    )

    val yandex: Service = Service.fromDto(
        id = ServiceId("yandex-go-id"),
        name = "Яндекс Go",
        siteUrl = "go.yandex",
        logoUrl = "https://img.logo.dev/go.yandex?token=pk_X-dlktBsQHWLGoIal-W1kA",
        promoCodeCount = 19,
    )

    val technodom: Service = Service.fromDto(
        id = ServiceId("technodom-id"),
        name = "Technodom",
        siteUrl = "technodom.kz",
        logoUrl = "https://img.logo.dev/technodom.kz?token=pk_X-dlktBsQHWLGoIal-W1kA",
        promoCodeCount = 23,
    )

    val spotify: Service = Service.fromDto(
        id = ServiceId("spotify-id"),
        name = "Spotify",
        siteUrl = "spotify.com",
        logoUrl = "https://img.logo.dev/spotify.com?token=pk_X-dlktBsQHWLGoIal-W1kA",
        promoCodeCount = 28,
    )

    val amazon: Service = Service.fromDto(
        id = ServiceId("amazon-id"),
        name = "Amazon",
        siteUrl = "amazon.com",
        logoUrl = "https://img.logo.dev/amazon.com?token=pk_X-dlktBsQHWLGoIal-W1kA",
        promoCodeCount = 156,
    )

    val withLongName: Service = Service.fromDto(
        id = ServiceId("long-name-id"),
        name = "Super Long Service Name Corporation Ltd",
        siteUrl = "custom.com",
        logoUrl = null,
        promoCodeCount = 0,
    )

    val allSamples = listOf(
        netflix,
        kaspi,
        glovo,
        yandex,
        technodom,
        spotify,
    )
}
