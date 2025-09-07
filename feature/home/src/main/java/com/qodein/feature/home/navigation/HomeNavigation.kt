package com.qodein.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.qodein.core.ui.scroll.ScrollStateRegistry
import com.qodein.feature.home.HomeScreen
import com.qodein.shared.model.Language
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import kotlinx.serialization.Serializable

@Serializable object HomeRoute // screen

@Serializable object HomeBaseRoute // route to base navigation graph

fun NavController.navigateToHome(navOptions: NavOptions) = navigate(route = HomeRoute, navOptions)

fun NavGraphBuilder.homeSection(
    userLanguage: Language,
    onPromoCodeClick: (PromoCode) -> Unit,
    promoCodeDetail: (PromoCodeId) -> Unit,
    scrollStateRegistry: ScrollStateRegistry
) {
    navigation<HomeBaseRoute>(startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                userLanguage = userLanguage,
                onNavigateToPromoCodeDetail = onPromoCodeClick,
                scrollStateRegistry = scrollStateRegistry,
            )
        }
    }
}
