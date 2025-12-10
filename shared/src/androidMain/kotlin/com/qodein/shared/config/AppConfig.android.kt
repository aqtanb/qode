package com.qodein.shared.config

import com.qodein.shared.BuildConfig

actual object AppConfig {
    actual val logoDevKey: String
        get() = BuildConfig.LOGO_DEV_PUBLIC_KEY
}
