package com.qodein.shared.ui

/**
 * Shared dialog types for filter functionality across features
 * Each feature can use the dialogs relevant to their data model
 */
enum class FilterDialogType {
    Category,
    Service,
    Tag,
    Sort
}

/**
 * Dialog coordinator interface that features can implement
 * Provides a consistent pattern for filter dialog management
 */
interface FilterDialogCoordinator {
    fun showFilterDialog(type: FilterDialogType)
    fun dismissFilterDialog()
}
