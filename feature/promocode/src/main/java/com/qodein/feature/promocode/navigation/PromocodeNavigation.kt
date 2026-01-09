package com.qodein.feature.promocode.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.navigation.ServiceSelectionResult
import com.qodein.feature.promocode.detail.PromocodeDetailRoute
import com.qodein.feature.promocode.submission.PromocodeSubmissionScreen
import com.qodein.feature.promocode.submission.PromocodeSubmissionViewModel
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.ServiceId
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

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
    onNavigateToAuth: (AuthPromptAction) -> Unit,
    onShowServiceSelection: (ServiceId?) -> Unit
) {
    composable<PromocodeSubmissionRoute> { backStackEntry ->
        val viewModel: PromocodeSubmissionViewModel = koinViewModel()

        LaunchedEffect(Unit) {
            backStackEntry.savedStateHandle
                .getStateFlow<List<String>?>(ServiceSelectionResult.KEY_SELECTED_SERVICE_IDS, null)
                .filterNotNull()
                .collect { selectedServiceIds ->
                    Logger.d("PromocodeNavigation") { "StateFlow emitted: $selectedServiceIds" }
                    val selectedServiceId = selectedServiceIds.firstOrNull()?.let { ServiceId(it) }
                    Logger.d("PromocodeNavigation") { "Parsed serviceId: $selectedServiceId" }
                    if (selectedServiceId != null) {
                        Logger.d("PromocodeNavigation") { "Calling viewModel.applyServiceSelection" }
                        viewModel.applyServiceSelection(selectedServiceId)
                    }

                    /*
                     * CRITICAL: Clear SavedStateHandle after processing to allow re-selecting same service
                     *
                     * BUG FIX: StateFlow.getStateFlow() only emits when the value CHANGES.
                     * If user selects BTV, then switches to manual entry (which clears the service in
                     * ViewModel but NOT in SavedStateHandle), then tries to select BTV again:
                     * - Without this clear: savedStateHandle.set([BTV]) does nothing (same value)
                     *   → StateFlow doesn't emit → applyServiceSelection never called → BTV not selected
                     * - With this clear: savedStateHandle goes null → [BTV] is a change
                     *   → StateFlow emits → applyServiceSelection called → BTV selected ✓
                     */
                    backStackEntry.savedStateHandle.set<List<String>?>(ServiceSelectionResult.KEY_SELECTED_SERVICE_IDS, null)
                    Logger.d("PromocodeNavigation") { "Processing complete, state cleared" }
                }
        }

        PromocodeSubmissionScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAuth = onNavigateToAuth,
            onShowServiceSelection = onShowServiceSelection,
            viewModel = viewModel,
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
