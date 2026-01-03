package com.qodein.feature.home.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.qodein.core.ui.navigation.ServiceSelectionResult
import com.qodein.core.ui.scroll.ScrollStateRegistry
import com.qodein.feature.home.HomeScreen
import com.qodein.feature.home.HomeViewModel
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.ServiceId
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Serializable object HomeRoute

@Serializable object HomeBaseRoute

fun NavGraphBuilder.homeSection(
    onPromoCodeClick: (PromocodeId) -> Unit,
    onShowServiceSelection: (Set<ServiceId>) -> Unit,
    scrollStateRegistry: ScrollStateRegistry
) {
    navigation<HomeBaseRoute>(startDestination = HomeRoute) {
        composable<HomeRoute> { backStackEntry ->
            val viewModel: HomeViewModel = koinViewModel()

            LaunchedEffect(Unit) {
                backStackEntry.savedStateHandle
                    .getStateFlow<List<String>?>(ServiceSelectionResult.KEY_SELECTED_SERVICE_IDS, null)
                    .filterNotNull()
                    .collectLatest { selectedServiceIds ->
                        Timber.d("HomeNavigation: Received ${selectedServiceIds.size} service IDs from savedStateHandle")
                        val serviceIds = selectedServiceIds.map { ServiceId(it) }.toSet()
                        viewModel.applyServiceSelection(serviceIds)
                        backStackEntry.savedStateHandle.remove<List<String>>(ServiceSelectionResult.KEY_SELECTED_SERVICE_IDS)
                    }
            }

            HomeScreen(
                onNavigateToPromoCodeDetail = onPromoCodeClick,
                onShowServiceSelection = onShowServiceSelection,
                scrollStateRegistry = scrollStateRegistry,
                viewModel = viewModel,
            )
        }
    }
}
