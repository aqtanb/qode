package com.qodein.feature.catalog.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store
import com.qodein.feature.catalog.CatalogScreen
import kotlinx.serialization.Serializable

/**
 * Catalog navigation routes using type-safe navigation with Kotlin Serialization
 */
@Serializable
object CatalogBaseRoute

@Serializable
object CatalogRoute

@Serializable
data class CatalogWithFilterRoute(val categoryId: String? = null, val storeId: String? = null, val searchQuery: String? = null)

/**
 * Extension function to navigate to catalog screen
 */
fun NavController.navigateToCatalog(navOptions: NavOptions? = null) {
    navigate(route = CatalogRoute, navOptions)
}

/**
 * Extension function to navigate to catalog with specific filters
 */
fun NavController.navigateToCatalogWithFilter(
    categoryId: String? = null,
    storeId: String? = null,
    searchQuery: String? = null,
    navOptions: NavOptions? = null
) {
    navigate(
        route = CatalogWithFilterRoute(
            categoryId = categoryId,
            storeId = storeId,
            searchQuery = searchQuery,
        ),
        navOptions = navOptions,
    )
}

/**
 * Navigation graph builder extension for catalog feature
 */
fun NavGraphBuilder.catalogSection(
    onPromoCodeClick: (PromoCode) -> Unit = {},
    onStoreClick: (Store) -> Unit = {},
    onCategoryClick: (Category) -> Unit = {},
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    // Base catalog route
    composable<CatalogBaseRoute> {
        CatalogScreen(
            onPromoCodeClick = onPromoCodeClick,
            onStoreClick = onStoreClick,
            onCategoryClick = onCategoryClick,
            showTopBar = false, // Use app's global top bar
        )
    }

    // Main catalog route
    composable<CatalogRoute> {
        CatalogScreen(
            onPromoCodeClick = onPromoCodeClick,
            onStoreClick = onStoreClick,
            onCategoryClick = onCategoryClick,
            showTopBar = false, // Use app's global top bar
        )
    }

    // Catalog with filters route
    composable<CatalogWithFilterRoute> { backStackEntry ->
        val route = backStackEntry.arguments?.let {
            // Extract route arguments if needed for pre-filtering
            CatalogWithFilterRoute(
                categoryId = it.getString("categoryId"),
                storeId = it.getString("storeId"),
                searchQuery = it.getString("searchQuery"),
            )
        }

        CatalogScreen(
            onPromoCodeClick = onPromoCodeClick,
            onStoreClick = onStoreClick,
            onCategoryClick = onCategoryClick,
            showTopBar = false, // Use app's global top bar
            // You could pass initial filter parameters to the ViewModel here
            // initialCategoryId = route?.categoryId,
            // initialStoreId = route?.storeId,
            // initialSearchQuery = route?.searchQuery
        )
    }
}
