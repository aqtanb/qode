package com.qodein.core.testing.data

import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId

object ServiceMother {

    private fun base() =
        Service.fromDto(
            id = ServiceId("default-service-id"),
            name = "Default Service",
            siteUrl = "example.com",
            logoUrl = null,
            promoCodeCount = 0,
        )

    fun netflix() =
        Service.fromDto(
            id = ServiceId("netflix-id"),
            name = "Netflix",
            siteUrl = "netflix.com",
            logoUrl = "https://img.logo.dev/netflix.com?token=pk_X-dlktBsQHWLGoIal-W1kA",
            promoCodeCount = 42,
        )

    fun spotify() =
        Service.fromDto(
            id = ServiceId("spotify-id"),
            name = "Spotify",
            siteUrl = "spotify.com",
            logoUrl = "https://img.logo.dev/spotify.com?token=pk_X-dlktBsQHWLGoIal-W1kA",
            promoCodeCount = 28,
        )

    fun amazon() =
        Service.fromDto(
            id = ServiceId("amazon-id"),
            name = "Amazon",
            siteUrl = "amazon.com",
            logoUrl = "https://img.logo.dev/amazon.com?token=pk_X-dlktBsQHWLGoIal-W1kA",
            promoCodeCount = 156,
        )

    fun kaspi() =
        Service.fromDto(
            id = ServiceId("kaspi-id"),
            name = "Kaspi.kz",
            siteUrl = "kaspi.kz",
            logoUrl = "https://img.logo.dev/kaspi.kz?token=pk_X-dlktBsQHWLGoIal-W1kA",
            promoCodeCount = 67,
        )

    fun glovo() =
        Service.fromDto(
            id = ServiceId("glovo-id"),
            name = "Glovo",
            siteUrl = "glovoapp.com",
            logoUrl = "https://img.logo.dev/glovoapp.com?token=pk_X-dlktBsQHWLGoIal-W1kA",
            promoCodeCount = 34,
        )

    fun yandexGo() =
        Service.fromDto(
            id = ServiceId("yandex-go-id"),
            name = "Яндекс Go",
            siteUrl = "go.yandex",
            logoUrl = "https://img.logo.dev/go.yandex?token=pk_X-dlktBsQHWLGoIal-W1kA",
            promoCodeCount = 19,
        )

    fun technodom() =
        Service.fromDto(
            id = ServiceId("technodom-id"),
            name = "Technodom",
            siteUrl = "technodom.kz",
            logoUrl = "https://img.logo.dev/technodom.kz?token=pk_X-dlktBsQHWLGoIal-W1kA",
            promoCodeCount = 23,
        )

    fun withMaxLengthName() =
        withCustom(
            id = "max-name-id",
            name = "A".repeat(Service.NAME_MAX_LENGTH),
        )

    fun withMinLengthName() =
        withCustom(
            id = "min-name-id",
            name = "A".repeat(Service.NAME_MIN_LENGTH),
        )

    fun withLongName() =
        withCustom(
            id = "long-name-id",
            name = "Super Long Service Name Corporation Ltd",
        )

    fun withZeroPromocodes() =
        withCustom(
            id = "zero-promo-id",
            name = "Zero Promocodes Service",
            promoCodeCount = 0,
        )

    fun withOnePromocode() =
        withCustom(
            id = "one-promo-id",
            name = "One Promocode Service",
            promoCodeCount = 1,
        )

    fun withManyPromocodes() =
        withCustom(
            id = "many-promo-id",
            name = "Many Promocodes Service",
            promoCodeCount = 9999,
        )

    fun withNullLogo() =
        withCustom(
            id = "null-logo-id",
            name = "No Logo Service",
            logoUrl = null,
        )

    fun withCustom(
        id: String = "custom-id",
        name: String = "Custom Service",
        siteUrl: String = "custom.com",
        logoUrl: String? = null,
        promoCodeCount: Int = 0
    ) = Service.fromDto(
        id = ServiceId(id),
        name = name,
        siteUrl = siteUrl,
        logoUrl = logoUrl,
        promoCodeCount = promoCodeCount,
    )

    fun sampleList() =
        listOf(
            netflix(),
            spotify(),
            amazon(),
            kaspi(),
            glovo(),
        )

    fun emptyList() = emptyList<Service>()
}
