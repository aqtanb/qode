package com.qodein.shared.platform

/**
 * Platform-agnostic URI representation.
 * Wraps platform-specific URI types (Android Uri, iOS URL).
 */
expect class PlatformUri {
    /**
     * String representation of the URI.
     */
    fun toString(): String
}
