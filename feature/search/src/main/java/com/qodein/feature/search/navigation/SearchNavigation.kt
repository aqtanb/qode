package com.qodein.feature.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store
import com.qodein.feature.search.SearchScreen
import kotlinx.serialization.Serializable

/**
 * Catalog navigation routes using type-safe navigation with Kotlin Serialization
 */
@Serializable
object SearchBaseRoute

@Serializable
object SearchRoute

@Serializable
data class SearchWithFilterRoute(val categoryId: String? = null, val storeId: String? = null, val searchQuery: String? = null)

/**
 * Extension function to navigate to catalog screen
 */
fun NavController.navigateToSearch(navOptions: NavOptions? = null) {
    navigate(route = SearchRoute, navOptions)
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
        route = SearchWithFilterRoute(
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
fun NavGraphBuilder.searchSection(
    onPromoCodeClick: (PromoCode) -> Unit = {},
    onStoreClick: (Store) -> Unit = {},
    onCategoryClick: (Category) -> Unit = {},
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    // Base catalog route
    composable<SearchBaseRoute> {
        SearchScreen(
            onPromoCodeClick = onPromoCodeClick,
            onStoreClick = onStoreClick,
            onCategoryClick = onCategoryClick,
            showTopBar = false, // Use app's global top bar
        )
    }

    // Main catalog route
    composable<SearchRoute> {
        SearchScreen(
            onPromoCodeClick = onPromoCodeClick,
            onStoreClick = onStoreClick,
            onCategoryClick = onCategoryClick,
            showTopBar = false, // Use app's global top bar
        )
    }

    // Catalog with filters route
    composable<SearchWithFilterRoute> { backStackEntry ->
        val route = backStackEntry.arguments?.let {
            // Extract route arguments if needed for pre-filtering
            SearchWithFilterRoute(
                categoryId = it.getString("categoryId"),
                storeId = it.getString("storeId"),
                searchQuery = it.getString("searchQuery"),
            )
        }

        SearchScreen(
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
