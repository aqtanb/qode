package com.qodein.feature.feed.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.qodein.feature.feed.FeedScreen
import kotlinx.serialization.Serializable

/**
 * Feed navigation routes using type-safe navigation with Kotlin Serialization
 */
@Serializable
object FeedRoute

/**
 * Extension function to navigate to feed screen
 */
fun NavController.navigateToFeed(navOptions: NavOptions? = null) {
    navigate(route = FeedRoute, navOptions)
}

/**
 * Navigation graph builder extension for feed feature
 */
fun NavGraphBuilder.feedSection() {
    composable<FeedRoute> {
        FeedScreen()
    }
}
