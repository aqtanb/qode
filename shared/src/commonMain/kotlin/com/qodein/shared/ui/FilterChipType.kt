package com.qodein.shared.ui

/**
 * Enum for filter chip types to avoid hardcoded string keys
 * Provides type-safe keys for filter chips in LazyRow
 */
enum class FilterChipType(val key: String) {
    Category("category_filter"),
    Service("service_filter"),
    Tag("tag_filter"),
    Sort("sort_filter")
}
