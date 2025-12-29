package com.qodein.feature.promocode.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.qodein.core.ui.AuthPromptAction
import com.qodein.feature.promocode.detail.PromocodeDetailRoute
import com.qodein.feature.promocode.submission.PromocodeSubmissionScreen
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.UserId
import kotlinx.serialization.Serializable

@Serializable object PromocodeSubmissionRoute

@Serializable data class PromocodeDetailRoute(val promoCodeId: String)

fun NavController.navigateToPromocodeSubmission(navOptions: NavOptions? = null) =
    navigate(route = PromocodeSubmissionRoute, navOptions = navOptions)

fun NavController.navigateToPromocodeDetail(
    promoCodeId: PromocodeId,
    navOptions: NavOptions? = null
) = navigate(route = PromocodeDetailRoute(promoCodeId.value), navOptions = navOptions)

fun NavGraphBuilder.promocodeSubmissionSection(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit
) {
    composable<PromocodeSubmissionRoute> {
        PromocodeSubmissionScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAuth = onNavigateToAuth,
        )
    }
}

fun NavGraphBuilder.promocodeDetailSection(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit,
    onNavigateToReport: (String, String, String?) -> Unit,
    onNavigateToBlockUser: (UserId, String?, String?) -> Unit
) {
    composable<PromocodeDetailRoute>(
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "https://qodein.web.app/promocodes/{promoCodeId}"
                action = "android.intent.action.VIEW"
            },
            navDeepLink {
                uriPattern = "qodein://promocode/{promoCodeId}"
                action = "android.intent.action.VIEW"
            },
        ),
    ) { backStackEntry ->
        val args = backStackEntry.toRoute<PromocodeDetailRoute>()
        PromocodeDetailRoute(
            promoCodeId = PromocodeId(args.promoCodeId),
            onNavigateBack = onNavigateBack,
            onNavigateToAuth = onNavigateToAuth,
            onNavigateToReport = onNavigateToReport,
            onNavigateToBlockUser = onNavigateToBlockUser,
        )
    }
}
