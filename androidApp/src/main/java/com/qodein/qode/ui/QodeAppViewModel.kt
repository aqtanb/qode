package com.qodein.qode.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.ui.state.AppUiEvents
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
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
class QodeAppViewModel @Inject constructor(getAuthStateUseCase: GetAuthStateUseCase) : ViewModel() {
    private val authStateFlow = getAuthStateUseCase()
        .catch { emit(AuthState.Unauthenticated) }

    val authState: StateFlow<AuthState> = authStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthState.Unauthenticated,
        )

    private val _navigationEvents = MutableSharedFlow<NavigationActions>()
    val navigationEvents: SharedFlow<NavigationActions> = _navigationEvents.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<AppUiEvents>()
    val uiEvents: SharedFlow<AppUiEvents> = _uiEvents.asSharedFlow()

    /**
     * Handle UI events (navigation + app-level UI events)
     */
    fun handleUiEvent(event: AppUiEvents) {
        viewModelScope.launch {
            when (event) {
                is AppUiEvents.Navigate -> {
                    // Delegate navigation events to the navigation flow
                    _navigationEvents.emit(event.action)
                }
                else -> {
                    // Other UI events (dialogs, etc.) go to UI events flow
                    _uiEvents.emit(event)
                }
            }
        }
    }
}
