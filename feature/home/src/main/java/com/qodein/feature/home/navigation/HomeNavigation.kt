package com.qodein.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.qodein.feature.home.HomeScreen
import com.qodein.feature.home.ui.HomeIconService
import com.qodein.shared.model.Language
import kotlinx.serialization.Serializable

@Serializable object HomeRoute // screen

@Serializable object HomeBaseRoute // route to base navigation graph

fun NavController.navigateToHome(navOptions: NavOptions) = navigate(route = HomeRoute, navOptions)

fun NavGraphBuilder.homeSection(
    userLanguage: Language,
    onPromoCodeClick: (String) -> Unit,
    promoCodeDetail: NavGraphBuilder.() -> Unit
) {
    navigation<HomeBaseRoute>(startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                userLanguage = userLanguage,
                iconService = HomeIconService(),
            )
        }
    }
}
