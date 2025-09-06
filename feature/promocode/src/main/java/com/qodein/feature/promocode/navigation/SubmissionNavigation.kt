package com.qodein.feature.promocode.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.qodein.feature.promocode.detail.PromocodeDetailScreen
import com.qodein.feature.promocode.submission.SubmissionScreen
import com.qodein.shared.model.PromoCodeId
import kotlinx.serialization.Serializable

@Serializable object SubmissionRoute

@Serializable data class PromocodeDetailRoute(val promoCodeId: String)

fun NavController.navigateToSubmission(navOptions: NavOptions? = null) = navigate(route = SubmissionRoute, navOptions = navOptions)

fun NavController.navigateToPromocodeDetail(
    promoCodeId: PromoCodeId,
    navOptions: NavOptions? = null
) = navigate(route = PromocodeDetailRoute(promoCodeId.value), navOptions = navOptions)

fun NavGraphBuilder.submissionSection(
    onNavigateBack: () -> Unit = {},
    isDarkTheme: Boolean
) {
    composable<SubmissionRoute> {
        SubmissionScreen(
            onNavigateBack = onNavigateBack,
        )
    }

    composable<PromocodeDetailRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<PromocodeDetailRoute>()
        PromocodeDetailScreen(
            promoCodeId = PromoCodeId(args.promoCodeId),
            onNavigateBack = onNavigateBack,
            onNavigateToComments = { promoCodeId ->
                // TODO: Navigate to comments
            },
            onNavigateToService = { serviceName ->
                // TODO: Navigate to service
            },
            isDarkTheme = isDarkTheme,
        )
    }
}

fun NavGraphBuilder.promocodeDetailSection(
    onNavigateBack: () -> Unit = {},
    onNavigateToComments: (PromoCodeId) -> Unit = {},
    onNavigateToService: (String) -> Unit = {},
    isDarkTheme: Boolean
) {
    composable<PromocodeDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<PromocodeDetailRoute>()
        PromocodeDetailScreen(
            promoCodeId = PromoCodeId(route.promoCodeId),
            onNavigateBack = onNavigateBack,
            onNavigateToComments = onNavigateToComments,
            onNavigateToService = onNavigateToService,
            isDarkTheme = isDarkTheme,
        )
    }
}
