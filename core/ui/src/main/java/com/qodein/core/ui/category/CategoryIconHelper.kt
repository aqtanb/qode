package com.qodein.core.ui.category

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.component.QodeColorScheme
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.shared.model.Service.Companion.Categories

/**
 * Android-specific category icon mapping utility.
 * Maps category strings to appropriate Material/QodeIcons.
 */
object CategoryIconHelper {

    /**
     * Category to icon mapping using existing QodeIcons
     */
    private val CATEGORY_ICONS: Map<String, ImageVector> = mapOf(
        Categories.STREAMING to QodeCategoryIcons.Streaming,
        Categories.FOOD to QodeCategoryIcons.Food,
        Categories.TRANSPORT to QodeCategoryIcons.Bus,
        Categories.SHOPPING to QodeCategoryIcons.Grocery,
        Categories.GAMING to QodeCategoryIcons.Computers,
        Categories.MUSIC to QodeCategoryIcons.Music,
        Categories.EDUCATION to QodeCategoryIcons.Education,
        Categories.FITNESS to QodeCategoryIcons.Fitness,
        Categories.FINANCE to QodeCategoryIcons.Finance,
        Categories.BEAUTY to QodeCategoryIcons.Cosmetics,
        Categories.CLOTHING to QodeCategoryIcons.Clothing,
        Categories.ELECTRONICS to QodeCategoryIcons.Electronics,
        Categories.TRAVEL to QodeCategoryIcons.Travel,
        Categories.PHARMACY to QodeCategoryIcons.Supplements,
        Categories.JEWELRY to QodeCategoryIcons.Accessories,
        Categories.HEALTH to QodeCategoryIcons.Medical,
        Categories.ENTERTAINMENT to QodeCategoryIcons.Entertainment,
        Categories.MARKETPLACE to QodeCategoryIcons.Investment,
        Categories.SERVICES to QodeCategoryIcons.Services,
        Categories.TELECOM to QodeCategoryIcons.Phones,
        Categories.OTHER to QodeCommerceIcons.Order,
        Categories.UNSPECIFIED to QodeNavigationIcons.Help,
    )

    /**
     * UI Category groups for better UX
     */
    enum class CategoryGroup(val displayName: String) {
        SHOPPING("Shopping"),
        MEDIA("Media"),
        LIFESTYLE("Lifestyle"),
        SERVICES("Services"),
        OTHER("Other")
    }

    /**
     * Category group to gradient color scheme mapping for beautiful banners
     */
    private val CATEGORY_GROUP_GRADIENTS: Map<CategoryGroup, QodeColorScheme> = mapOf(
        CategoryGroup.SHOPPING to QodeColorScheme.BannerOrange,
        CategoryGroup.MEDIA to QodeColorScheme.BannerPurple,
        CategoryGroup.LIFESTYLE to QodeColorScheme.BannerGreen,
        CategoryGroup.SERVICES to QodeColorScheme.BannerCyan,
        CategoryGroup.OTHER to QodeColorScheme.BannerIndigo,
    )

    /**
     * Category to group mapping for UI organization
     */
    private val CATEGORY_GROUPS: Map<String, CategoryGroup> = mapOf(
        // Shopping group
        Categories.SHOPPING to CategoryGroup.SHOPPING,
        Categories.MARKETPLACE to CategoryGroup.SHOPPING,
        Categories.CLOTHING to CategoryGroup.SHOPPING,
        Categories.JEWELRY to CategoryGroup.SHOPPING,
        Categories.ELECTRONICS to CategoryGroup.SHOPPING,

        // Media group
        Categories.STREAMING to CategoryGroup.MEDIA,
        Categories.MUSIC to CategoryGroup.MEDIA,
        Categories.GAMING to CategoryGroup.MEDIA,
        Categories.ENTERTAINMENT to CategoryGroup.MEDIA,

        // Lifestyle group
        Categories.FOOD to CategoryGroup.LIFESTYLE,
        Categories.BEAUTY to CategoryGroup.LIFESTYLE,
        Categories.HEALTH to CategoryGroup.LIFESTYLE,
        Categories.PHARMACY to CategoryGroup.LIFESTYLE,
        Categories.FITNESS to CategoryGroup.LIFESTYLE,

        // Services group
        Categories.TRANSPORT to CategoryGroup.SERVICES,
        Categories.FINANCE to CategoryGroup.SERVICES,
        Categories.EDUCATION to CategoryGroup.SERVICES,
        Categories.TELECOM to CategoryGroup.SERVICES,
        Categories.SERVICES to CategoryGroup.SERVICES,

        // Other group
        Categories.TRAVEL to CategoryGroup.OTHER,
        Categories.OTHER to CategoryGroup.OTHER,
        Categories.UNSPECIFIED to CategoryGroup.OTHER,
    )

    /**
     * Gets icon for category
     */
    @Composable
    fun getCategoryIcon(category: String?): ImageVector = CATEGORY_ICONS[category] ?: CATEGORY_ICONS[Categories.OTHER]!!

    /**
     * Gets category group for UI organization
     */
    fun getCategoryGroup(category: String?): CategoryGroup = CATEGORY_GROUPS[category] ?: CategoryGroup.OTHER

    /**
     * Gets categories by group
     */
    fun getCategoriesByGroup(group: CategoryGroup): List<String> =
        CATEGORY_GROUPS.entries
            .filter { it.value == group }
            .map { it.key }

    /**
     * Gets all category groups with their categories
     */
    fun getCategoryGroups(): Map<CategoryGroup, List<String>> =
        CategoryGroup.entries.associateWith { group ->
            getCategoriesByGroup(group)
        }

    /**
     * Gets gradient color scheme for category banners based on category group
     */
    fun getCategoryGradient(category: String?): QodeColorScheme {
        val categoryGroup = getCategoryGroup(category)
        return CATEGORY_GROUP_GRADIENTS[categoryGroup] ?: QodeColorScheme.BannerIndigo
    }

    /**
     * Gets gradient color scheme for category group directly
     */
    fun getCategoryGroupGradient(group: CategoryGroup): QodeColorScheme = CATEGORY_GROUP_GRADIENTS[group] ?: QodeColorScheme.BannerIndigo

    /**
     * Checks if category has icon mapping
     */
    fun hasIcon(category: String?): Boolean = CATEGORY_ICONS.containsKey(category)
}
