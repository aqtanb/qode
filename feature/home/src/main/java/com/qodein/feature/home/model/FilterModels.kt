package com.qodein.feature.home.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.model.Service

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

@Stable
sealed class ServiceFilter {
    data object All : ServiceFilter()
    data class Selected(val services: Set<Service>) : ServiceFilter() {
        val isEmpty: Boolean get() = services.isEmpty()

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

@Stable
sealed class SortFilter {
    data class Selected(val sortBy: PromoCodeSortBy) : SortFilter()
}

@Stable
data class FilterState(
    val categoryFilter: CategoryFilter = CategoryFilter.All,
    val serviceFilter: ServiceFilter = ServiceFilter.All,
    val sortFilter: SortFilter = SortFilter.Selected(PromoCodeSortBy.POPULARITY)
) {
    val hasActiveFilters: Boolean
        get() = categoryFilter !is CategoryFilter.All ||
            serviceFilter !is ServiceFilter.All

    fun reset(): FilterState = FilterState()
}

@Stable
sealed class FilterDialogType {
    data object Category : FilterDialogType()
    data object Service : FilterDialogType()
    data object Sort : FilterDialogType()
}

// Extension functions for dynamic UI
fun PromoCodeSortBy.toIcon(): ImageVector =
    when (this) {
        PromoCodeSortBy.POPULARITY -> QodeStatusIcons.Popular
        PromoCodeSortBy.NEWEST -> QodeStatusIcons.New
        PromoCodeSortBy.EXPIRING_SOON -> QodeCommerceIcons.Limited
    }

fun PromoCodeSortBy.toSectionTitleRes(): Int =
    when (this) {
        PromoCodeSortBy.POPULARITY -> com.qodein.feature.home.R.string.home_section_title_popularity
        PromoCodeSortBy.NEWEST -> com.qodein.feature.home.R.string.home_section_title_newest
        PromoCodeSortBy.EXPIRING_SOON -> com.qodein.feature.home.R.string.home_section_title_expiring
    }

// TODO: Make sure categories have single source of truth
fun String.toCategoryIcon(): ImageVector =
    when (this.lowercase()) {
        "food" -> QodeCategoryIcons.Food
        "restaurant" -> QodeCategoryIcons.Restaurant
        "fastfood" -> QodeCategoryIcons.FastFood
        "coffee" -> QodeCategoryIcons.Coffee
        "cafe" -> QodeCategoryIcons.Restaurant
        "electronics" -> QodeCategoryIcons.Electronics
        "tech" -> QodeCategoryIcons.Computers
        "fashion" -> QodeCategoryIcons.Fashion
        "clothing" -> QodeCategoryIcons.Clothing
        "beauty" -> QodeCategoryIcons.Cosmetics
        "cosmetics" -> QodeCategoryIcons.Makeup
        "travel" -> QodeCategoryIcons.Travel
        "hotel" -> QodeCategoryIcons.Hotel
        "fitness" -> QodeCategoryIcons.Fitness
        "gym" -> QodeCategoryIcons.Sports
        "shopping" -> QodeCategoryIcons.Grocery
        "delivery" -> QodeCategoryIcons.Delivery
        "bank" -> QodeCategoryIcons.Banking
        "financial" -> QodeCategoryIcons.Finance
        "entertainment" -> QodeCategoryIcons.Entertainment
        "education" -> QodeCategoryIcons.Education
        "health" -> QodeCategoryIcons.Medical
        "sports" -> QodeCategoryIcons.Sports
        "home" -> QodeCategoryIcons.Home
        else -> QodeCommerceIcons.Order
    }

// For services, we either show logoUrl or initials
data class ServiceIconData(val logoUrl: String? = null, val fallbackText: String? = null)

fun Service.getServiceIconData(): ServiceIconData {
    val serviceInitials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")
        .ifEmpty { name.take(2).uppercase() }

    return ServiceIconData(
        logoUrl = logoUrl,
        fallbackText = serviceInitials,
    )
}
