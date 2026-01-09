package com.qodein.feature.service.selection.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import com.qodein.core.ui.navigation.ServiceSelectionResult
import com.qodein.feature.service.selection.ServiceSelectionBottomSheet
import com.qodein.feature.service.selection.ServiceSelectionViewModel
import com.qodein.shared.model.ServiceId
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data class ServiceSelectionRoute(val initialServiceIds: List<String> = emptyList(), val isSingleSelection: Boolean = false)

fun SavedStateHandle.setServiceSelectionResult(serviceIds: Set<ServiceId>) {
    set(ServiceSelectionResult.KEY_SELECTED_SERVICE_IDS, serviceIds.map { it.value })
}

fun SavedStateHandle.getServiceSelectionResult(): Set<ServiceId>? =
    get<List<String>>(ServiceSelectionResult.KEY_SELECTED_SERVICE_IDS)
        ?.map { ServiceId(it) }
        ?.toSet()

fun NavController.navigateToServiceSelection(
    initialServiceIds: Set<ServiceId> = emptySet(),
    isSingleSelection: Boolean = false,
    navOptions: NavOptions? = null
) {
    navigate(
        route = ServiceSelectionRoute(
            initialServiceIds = initialServiceIds.map { it.value },
            isSingleSelection = isSingleSelection,
        ),
        navOptions = navOptions,
    )
}

fun NavGraphBuilder.serviceSelectionSection(
    onNavigateBack: () -> Unit,
    onResult: (Set<ServiceId>) -> Unit
) {
    dialog<ServiceSelectionRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<ServiceSelectionRoute>()
        val viewModel: ServiceSelectionViewModel = koinViewModel(viewModelStoreOwner = backStackEntry)

        LaunchedEffect(Unit) {
            Logger.d("ServiceSelectionNavigation") {
                "Initializing with ${args.initialServiceIds.size} services, singleSelection=${args.isSingleSelection}"
            }
            viewModel.initialize(
                initialServiceIds = args.initialServiceIds.map { ServiceId(it) }.toSet(),
                isSingleSelection = args.isSingleSelection,
            )
        }

        ServiceSelectionBottomSheet(
            viewModel = viewModel,
            onDismiss = { selectedIds ->
                Logger.d("ServiceSelectionNavigation") { "onDismiss callback received with ${selectedIds.size} services" }
                onResult(selectedIds)
                Logger.d("ServiceSelectionNavigation") { "onResult called, now calling onNavigateBack" }
                onNavigateBack()
            },
        )
    }
}
