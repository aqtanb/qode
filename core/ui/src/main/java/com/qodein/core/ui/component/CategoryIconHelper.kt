package com.qodein.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.component.QodeColorScheme
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.lifestyleGradientEnd
import com.qodein.core.designsystem.theme.lifestyleGradientStart
import com.qodein.core.designsystem.theme.mediaGradientEnd
import com.qodein.core.designsystem.theme.mediaGradientStart
import com.qodein.core.designsystem.theme.primaryGradientEnd
import com.qodein.core.designsystem.theme.primaryGradientStart
import com.qodein.core.designsystem.theme.servicesGradientEnd
import com.qodein.core.designsystem.theme.servicesGradientStart
import com.qodein.core.designsystem.theme.shoppingGradientEnd
import com.qodein.core.designsystem.theme.shoppingGradientStart
import com.qodein.shared.model.Service
import kotlin.collections.get

/**
 * Android-specific category icon mapping utility.
 * Maps category strings to appropriate Material/QodeIcons.
 */
object CategoryIconHelper {

    /**
     * Category to icon mapping using existing QodeIcons
     */
    private val CATEGORY_ICONS: Map<String, ImageVector> = mapOf(
        Service.Companion.Categories.STREAMING to QodeCategoryIcons.Streaming,
        Service.Companion.Categories.FOOD to QodeCategoryIcons.Food,
        Service.Companion.Categories.TRANSPORT to QodeCategoryIcons.Bus,
        Service.Companion.Categories.SHOPPING to QodeCategoryIcons.Grocery,
        Service.Companion.Categories.GAMING to QodeCategoryIcons.Computers,
        Service.Companion.Categories.MUSIC to QodeCategoryIcons.Music,
        Service.Companion.Categories.EDUCATION to QodeCategoryIcons.Education,
        Service.Companion.Categories.FITNESS to QodeCategoryIcons.Fitness,
        Service.Companion.Categories.FINANCE to QodeCategoryIcons.Finance,
        Service.Companion.Categories.BEAUTY to QodeCategoryIcons.Cosmetics,
        Service.Companion.Categories.CLOTHING to QodeCategoryIcons.Clothing,
        Service.Companion.Categories.ELECTRONICS to QodeCategoryIcons.Electronics,
        Service.Companion.Categories.TRAVEL to QodeCategoryIcons.Travel,
        Service.Companion.Categories.PHARMACY to QodeCategoryIcons.Supplements,
        Service.Companion.Categories.JEWELRY to QodeCategoryIcons.Accessories,
        Service.Companion.Categories.HEALTH to QodeCategoryIcons.Medical,
        Service.Companion.Categories.ENTERTAINMENT to QodeCategoryIcons.Entertainment,
        Service.Companion.Categories.MARKETPLACE to QodeCategoryIcons.Investment,
        Service.Companion.Categories.SERVICES to QodeCategoryIcons.Services,
        Service.Companion.Categories.TELECOM to QodeCategoryIcons.Phones,
        Service.Companion.Categories.OTHER to QodeCommerceIcons.Order,
        Service.Companion.Categories.UNSPECIFIED to QodeNavigationIcons.Help,
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
        Service.Companion.Categories.SHOPPING to CategoryGroup.SHOPPING,
        Service.Companion.Categories.MARKETPLACE to CategoryGroup.SHOPPING,
        Service.Companion.Categories.CLOTHING to CategoryGroup.SHOPPING,
        Service.Companion.Categories.JEWELRY to CategoryGroup.SHOPPING,
        Service.Companion.Categories.ELECTRONICS to CategoryGroup.SHOPPING,

        // Media group
        Service.Companion.Categories.STREAMING to CategoryGroup.MEDIA,
        Service.Companion.Categories.MUSIC to CategoryGroup.MEDIA,
        Service.Companion.Categories.GAMING to CategoryGroup.MEDIA,
        Service.Companion.Categories.ENTERTAINMENT to CategoryGroup.MEDIA,

        // Lifestyle group
        Service.Companion.Categories.FOOD to CategoryGroup.LIFESTYLE,
        Service.Companion.Categories.BEAUTY to CategoryGroup.LIFESTYLE,
        Service.Companion.Categories.HEALTH to CategoryGroup.LIFESTYLE,
        Service.Companion.Categories.PHARMACY to CategoryGroup.LIFESTYLE,
        Service.Companion.Categories.FITNESS to CategoryGroup.LIFESTYLE,

        // Services group
        Service.Companion.Categories.TRANSPORT to CategoryGroup.SERVICES,
        Service.Companion.Categories.FINANCE to CategoryGroup.SERVICES,
        Service.Companion.Categories.EDUCATION to CategoryGroup.SERVICES,
        Service.Companion.Categories.TELECOM to CategoryGroup.SERVICES,
        Service.Companion.Categories.SERVICES to CategoryGroup.SERVICES,

        // Other group
        Service.Companion.Categories.TRAVEL to CategoryGroup.OTHER,
        Service.Companion.Categories.OTHER to CategoryGroup.OTHER,
        Service.Companion.Categories.UNSPECIFIED to CategoryGroup.OTHER,
    )

    /**
     * Gets icon for category
     */
    @Composable
    fun getCategoryIcon(category: String?): ImageVector = CATEGORY_ICONS[category] ?: CATEGORY_ICONS[Service.Companion.Categories.OTHER]!!

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

    /**
     * Gets gradient colors for category based on category group
     * Returns List<Color> for reusability across the app
     */
    fun getCategoryGradientColors(category: String?): List<Color> {
        val categoryGroup = getCategoryGroup(category)
        return when (categoryGroup) {
            CategoryGroup.SHOPPING -> listOf(shoppingGradientStart, shoppingGradientEnd)
            CategoryGroup.MEDIA -> listOf(mediaGradientStart, mediaGradientEnd)
            CategoryGroup.LIFESTYLE -> listOf(lifestyleGradientStart, lifestyleGradientEnd)
            CategoryGroup.SERVICES -> listOf(servicesGradientStart, servicesGradientEnd)
            CategoryGroup.OTHER -> listOf(primaryGradientStart, primaryGradientEnd)
        }
    }
}
