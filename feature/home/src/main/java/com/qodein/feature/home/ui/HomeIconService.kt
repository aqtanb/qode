package com.qodein.feature.home.ui

import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.model.Service
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized icon mapping service for all home feature UI elements
 * Single source of truth for categories, services, filters, and sort icons
 */
@Singleton
class HomeIconService @Inject constructor() {

    // MARK: - Category Icons

    /**
     * Maps category name to appropriate icon
     */
    fun getCategoryIcon(categoryName: String): ImageVector = CATEGORY_ICONS[categoryName.lowercase()] ?: DEFAULT_CATEGORY_ICON

    /**
     * Gets all available categories with their icons
     */
    fun getAvailableCategories(): Map<String, ImageVector> = CATEGORY_ICONS

    /**
     * Validates if category name is supported
     */
    fun isCategorySupported(categoryName: String): Boolean = CATEGORY_ICONS.containsKey(categoryName.lowercase())

    // MARK: - Service Icons

    /**
     * Gets service icon data for UI display
     */
    fun getServiceIconData(service: Service): ServiceIconData {
        val serviceInitials = service.name.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
            .ifEmpty { service.name.take(2).uppercase() }

        return ServiceIconData(
            logoUrl = service.logoUrl,
            fallbackText = serviceInitials,
        )
    }

    // MARK: - Filter Icons

    /**
     * Gets icon for sort filter type
     */
    fun getSortIcon(sortBy: PromoCodeSortBy): ImageVector =
        when (sortBy) {
            PromoCodeSortBy.POPULARITY -> QodeStatusIcons.Popular
            PromoCodeSortBy.NEWEST -> QodeStatusIcons.New
            PromoCodeSortBy.EXPIRING_SOON -> QodeCommerceIcons.Limited
        }

    /**
     * Gets section title resource for sort type
     */
    fun getSortSectionTitleRes(sortBy: PromoCodeSortBy): Int =
        when (sortBy) {
            PromoCodeSortBy.POPULARITY -> com.qodein.feature.home.R.string.home_section_title_popularity
            PromoCodeSortBy.NEWEST -> com.qodein.feature.home.R.string.home_section_title_newest
            PromoCodeSortBy.EXPIRING_SOON -> com.qodein.feature.home.R.string.home_section_title_expiring
        }

    // MARK: - Private Constants

    companion object {
        private val CATEGORY_ICONS = mapOf(
            "food" to QodeCategoryIcons.Food,
            "restaurant" to QodeCategoryIcons.Restaurant,
            "fastfood" to QodeCategoryIcons.FastFood,
            "coffee" to QodeCategoryIcons.Coffee,
            "cafe" to QodeCategoryIcons.Restaurant,
            "electronics" to QodeCategoryIcons.Electronics,
            "tech" to QodeCategoryIcons.Computers,
            "fashion" to QodeCategoryIcons.Fashion,
            "clothing" to QodeCategoryIcons.Clothing,
            "beauty" to QodeCategoryIcons.Cosmetics,
            "cosmetics" to QodeCategoryIcons.Makeup,
            "travel" to QodeCategoryIcons.Travel,
            "hotel" to QodeCategoryIcons.Hotel,
            "fitness" to QodeCategoryIcons.Fitness,
            "gym" to QodeCategoryIcons.Sports,
            "shopping" to QodeCategoryIcons.Grocery,
            "delivery" to QodeCategoryIcons.Delivery,
            "bank" to QodeCategoryIcons.Banking,
            "financial" to QodeCategoryIcons.Finance,
            "entertainment" to QodeCategoryIcons.Entertainment,
            "education" to QodeCategoryIcons.Education,
            "health" to QodeCategoryIcons.Medical,
            "sports" to QodeCategoryIcons.Sports,
            "home" to QodeCategoryIcons.Home,
        )

        private val DEFAULT_CATEGORY_ICON = QodeCommerceIcons.Order
    }
}

/**
 * UI helper for service icon display
 */
data class ServiceIconData(val logoUrl: String? = null, val fallbackText: String? = null)
