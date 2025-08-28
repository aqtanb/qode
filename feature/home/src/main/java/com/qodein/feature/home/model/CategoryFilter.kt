package com.qodein.feature.home.model

import androidx.compose.runtime.Stable

@Stable
sealed class CategoryFilter {
    data object All : CategoryFilter()
    data class Selected(val categories: Set<String>) : CategoryFilter() {
        val isEmpty: Boolean get() = categories.isEmpty()

        fun toggle(category: String): CategoryFilter {
            val newCategories = if (categories.contains(category)) {
                categories - category
            } else {
                categories + category
            }
            return if (newCategories.isEmpty()) All else Selected(newCategories)
        }

        fun contains(category: String): Boolean = categories.contains(category)
    }
}
