package com.qodein.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.qodein.core.ui.scroll.ScrollStateRegistry
import com.qodein.feature.home.HomeScreen
import com.qodein.shared.model.PromocodeId
import kotlinx.serialization.Serializable

@Serializable object HomeRoute // screen

@Serializable object HomeBaseRoute // route to base navigation graph

fun NavGraphBuilder.homeSection(
    onPromoCodeClick: (PromocodeId) -> Unit,
    scrollStateRegistry: ScrollStateRegistry
) {
    navigation<HomeBaseRoute>(startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToPromoCodeDetail = onPromoCodeClick,
                scrollStateRegistry = scrollStateRegistry,
            )
        }
    }
}
