package com.qodein.feature.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.qodein.feature.search.SearchScreen
import kotlinx.serialization.Serializable

/**
 * Search navigation routes using type-safe navigation with Kotlin Serialization
 */
@Serializable
object SearchBaseRoute

@Serializable
object SearchRoute

/**
 * Extension function to navigate to search screen
 */
fun NavController.navigateToSearch(navOptions: NavOptions? = null) {
    navigate(route = SearchRoute, navOptions)
}

/**
 * Navigation graph builder extension for search feature
 */
fun NavGraphBuilder.searchSection() {
    // Base search route
    composable<SearchBaseRoute> {
        SearchScreen()
    }

    // Main search route
    composable<SearchRoute> {
        SearchScreen()
    }
}
