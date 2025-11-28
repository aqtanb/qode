package com.qodein.feature.promocode.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.qodein.feature.promocode.detail.PromocodeDetailScreen
import com.qodein.feature.promocode.submission.PromocodeSubmissionScreen
import com.qodein.shared.model.PromocodeId
import kotlinx.serialization.Serializable

@Serializable object PromocodeSubmissionRoute

@Serializable data class PromocodeDetailRoute(val promoCodeId: String)

fun NavController.navigateToPromocodeSubmission(navOptions: NavOptions? = null) =
    navigate(route = PromocodeSubmissionRoute, navOptions = navOptions)

fun NavController.navigateToPromocodeDetail(
    promoCodeId: PromocodeId,
    navOptions: NavOptions? = null
) = navigate(route = PromocodeDetailRoute(promoCodeId.value), navOptions = navOptions)

fun NavGraphBuilder.promocodeSubmissionSection(onNavigateBack: () -> Unit) {
    composable<PromocodeSubmissionRoute> {
        PromocodeSubmissionScreen(
            onNavigateBack = onNavigateBack,
        )
    }
}

fun NavGraphBuilder.promocodeDetailSection(onNavigateBack: () -> Unit) {
    composable<PromocodeDetailRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<PromocodeDetailRoute>()
        PromocodeDetailScreen(
            promoCodeId = PromocodeId(args.promoCodeId),
            onNavigateBack = onNavigateBack,
            onNavigateToComments = { promoCodeId ->
                // TODO: Navigate to comments
            },
            onNavigateToService = { serviceName ->
                // TODO: Navigate to service
            },
        )
    }
}
