package com.qodein.feature.auth.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.qodein.core.data.mapper.toDomain
import com.qodein.core.ui.util.PhoneUtils
import com.qodein.feature.auth.AuthAction
import com.qodein.feature.auth.AuthScreen
import com.qodein.feature.auth.AuthViewModel
import com.qodein.feature.auth.CountryPickerScreen
import kotlinx.serialization.Serializable

const val COUNTRY_CODE_RESULT_KEY = "selected_country_code"

@Serializable object AuthGraphRoute

@Serializable object AuthRoute

@Serializable
data class CountryPickerRoute(val currentCountryCode: String? = null)

// NavController: do what, NavOptions: how
fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    navigate(route = AuthRoute, navOptions = navOptions)
}

fun NavController.navigateToCountryPicker(currentCountryCode: String? = null) {
    navigate(route = CountryPickerRoute(currentCountryCode))
}

// NavGraphBuilder: what
fun NavGraphBuilder.authSection(
    onNavigateToCountryPicker: () -> Unit,
    onBackFromCountryPicker: () -> Unit
) {
    navigation<AuthGraphRoute>(startDestination = AuthRoute) {
        var authBackStackEntry: NavBackStackEntry? = null

        composable<AuthRoute> { backStackEntry ->
            authBackStackEntry = backStackEntry
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)

            // Observe country code selection result from SavedStateHandle
            val selectedCountryCode = backStackEntry.savedStateHandle
                .getStateFlow<String?>(COUNTRY_CODE_RESULT_KEY, null)
                .collectAsStateWithLifecycle()

            // Handle country selection when it changes
            LaunchedEffect(selectedCountryCode.value) {
                selectedCountryCode.value?.let { countryCode ->
                    // Convert country code back to Country object
                    val country = PhoneUtils.getCountryByCode(countryCode)?.toDomain()
                    country?.let {
                        authViewModel.handleAction(AuthAction.CountrySelected(it))
                        // Clear the result to avoid re-triggering
                        backStackEntry.savedStateHandle[COUNTRY_CODE_RESULT_KEY] = null
                    }
                }
            }

            AuthScreen(
                onNavigateToCountryPicker = onNavigateToCountryPicker,
                viewModel = authViewModel,
            )
        }
        composable<CountryPickerRoute> { navBackStackEntry ->
            CountryPickerScreen(
                onBack = onBackFromCountryPicker,
                onCountrySelected = { country ->
                    // Save country code to the auth back stack entry SavedStateHandle
                    authBackStackEntry?.savedStateHandle?.set(COUNTRY_CODE_RESULT_KEY, country.code)
                    onBackFromCountryPicker()
                },
            )
        }
    }
}
