package com.qodein.shared.platform

actual class PlatformUri(val url: String) {
    actual override fun toString(): String = url
}
