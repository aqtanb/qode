package com.qodein.shared.config

import com.qodein.shared.BuildKonfig

actual object AppConfig {
    actual val logoDevKey: String
        get() = BuildKonfig.LOGO_DEV_PUBLIC_KEY
}
