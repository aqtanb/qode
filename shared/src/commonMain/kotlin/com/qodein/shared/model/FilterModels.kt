package com.qodein.shared.model

/**
 * Filter type enumeration for different filter categories
 */
enum class FilterType {
    Category,
    Service,
    Tag,
    Sort
}

/**
 * Base interface for all filter types
 */
sealed interface FilterState {
    val isEmpty: Boolean
}

/**
 * Category filter for content filtering
 */
sealed class CategoryFilter : FilterState {
    data object All : CategoryFilter() {
        override val isEmpty: Boolean = true
    }

    data class Selected(val categories: Set<String>) : CategoryFilter() {
        override val isEmpty: Boolean get() = categories.isEmpty()

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

/**
 * Service filter for content filtering
 */
sealed class ServiceFilter : FilterState {
    data object All : ServiceFilter() {
        override val isEmpty: Boolean = true
    }

    data class Selected(val services: Set<Service>) : ServiceFilter() {
        override val isEmpty: Boolean get() = services.isEmpty()

        fun toggle(service: Service): ServiceFilter {
            val newServices = if (services.contains(service)) {
                services - service
            } else {
                services + service
            }
            return if (newServices.isEmpty()) All else Selected(newServices)
        }

        fun contains(service: Service): Boolean = services.contains(service)
    }
}

/**
 * Tag filter for content filtering (mainly for posts)
 */
sealed class TagFilter : FilterState {
    data object All : TagFilter() {
        override val isEmpty: Boolean = true
    }

    data class Selected(val tags: Set<Tag>) : TagFilter() {
        override val isEmpty: Boolean get() = tags.isEmpty()

        fun toggle(tag: Tag): TagFilter {
            val newTags = if (tags.contains(tag)) {
                tags - tag
            } else {
                tags + tag
            }
            return if (newTags.isEmpty()) All else Selected(newTags)
        }

        fun contains(tag: Tag): Boolean = tags.contains(tag)
    }
}

/**
 * Sort filter for content ordering
 */
data class SortFilter(val sortBy: ContentSortBy) : FilterState {
    override val isEmpty: Boolean = false // Sort is always selected
}

/**
 * Complete filter state for a feature
 */
data class CompleteFilterState(
    val categoryFilter: CategoryFilter = CategoryFilter.All,
    val serviceFilter: ServiceFilter = ServiceFilter.All,
    val tagFilter: TagFilter = TagFilter.All,
    val sortFilter: SortFilter = SortFilter(ContentSortBy.POPULARITY)
) {
    val hasActiveFilters: Boolean
        get() = !categoryFilter.isEmpty || !serviceFilter.isEmpty || !tagFilter.isEmpty

    /**
     * Apply category filter with validation
     * Categories and services are mutually exclusive
     */
    fun applyCategoryFilter(categoryFilter: CategoryFilter): CompleteFilterState =
        if (categoryFilter is CategoryFilter.Selected && categoryFilter.categories.isNotEmpty()) {
            // Clear service filter when category is selected
            copy(categoryFilter = categoryFilter, serviceFilter = ServiceFilter.All)
        } else {
            copy(categoryFilter = categoryFilter)
        }

    /**
     * Apply service filter with validation
     * Categories and services are mutually exclusive
     */
    fun applyServiceFilter(serviceFilter: ServiceFilter): CompleteFilterState =
        if (serviceFilter is ServiceFilter.Selected && serviceFilter.services.isNotEmpty()) {
            // Clear category filter when service is selected
            copy(serviceFilter = serviceFilter, categoryFilter = CategoryFilter.All)
        } else {
            copy(serviceFilter = serviceFilter)
        }

    /**
     * Apply sort filter
     */
    fun applySortFilter(sortFilter: SortFilter): CompleteFilterState = copy(sortFilter = sortFilter)

    /**
     * Apply tag filter
     */
    fun applyTagFilter(tagFilter: TagFilter): CompleteFilterState = copy(tagFilter = tagFilter)

    /**
     * Reset all filters to default state
     */
    fun reset(): CompleteFilterState = CompleteFilterState()

    /**
     * Validate filter state
     * Ensures category and service filters are not both active
     */
    fun isValid(): Boolean {
        val hasCategoryFilter = categoryFilter !is CategoryFilter.All && !categoryFilter.isEmpty
        val hasServiceFilter = serviceFilter !is ServiceFilter.All && !serviceFilter.isEmpty
        // Category and service filters cannot both be active
        return !(hasCategoryFilter && hasServiceFilter)
    }
}
