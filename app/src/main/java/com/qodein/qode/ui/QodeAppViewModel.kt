package com.qodein.qode.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.domain.AuthState
import com.qodein.core.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.navigation.NavigationHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enhanced ViewModel for app-level state management and navigation coordination.
 *
 * Responsibilities:
 * - Manages app-wide auth state via domain layer
 * - Coordinates type-safe navigation actions
 * - Provides clean interface between UI and domain
 *
 * Following enterprise patterns with proper separation of concerns.
 */
@HiltViewModel
class QodeAppViewModel @Inject constructor(getAuthStateUseCase: GetAuthStateUseCase, private val navigationHandler: NavigationHandler) :
    ViewModel() {

    // Auth state from domain layer with proper error handling
    val authState: StateFlow<AuthState> = getAuthStateUseCase()
        .map { result ->
            result.getOrElse { AuthState.Unauthenticated }
        }
        .catch { emit(AuthState.Unauthenticated) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthState.Loading,
        )

    // Navigation events flow
    private val _navigationEvents = MutableSharedFlow<NavigationActions>()
    val navigationEvents: SharedFlow<NavigationActions> = _navigationEvents.asSharedFlow()

    /**
     * Handle navigation action through centralized handler
     */
    fun handleNavigation(action: NavigationActions) {
        viewModelScope.launch {
            _navigationEvents.emit(action)
        }
    }

    /**
     * Get appropriate navigation action for profile click based on auth state
     */
    fun getProfileNavigationAction(): NavigationActions = navigationHandler.getProfileNavigationAction(authState.value)
}
