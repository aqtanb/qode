package com.qodein.qode.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.ui.state.AppUiEvents
import com.qodein.shared.common.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.preferences.GetThemeUseCase
import com.qodein.shared.domain.usecase.preferences.ObserveLanguageUseCase
import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme
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
class QodeAppViewModel @Inject constructor(
    getAuthStateUseCase: GetAuthStateUseCase,
    getThemeUseCase: GetThemeUseCase,
    observeLanguageUseCase: ObserveLanguageUseCase
) : ViewModel() {
    val authState: StateFlow<AuthState> = getAuthStateUseCase()
        .catch { emit(AuthState.Unauthenticated) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthState.Unauthenticated,
        )

    // Theme state from domain layer with proper error handling
    val themeState: StateFlow<Theme> = getThemeUseCase()
        .map { result ->
            when (result) {
                is Result.Success -> result.data
                is Result.Error -> Theme.SYSTEM
            }
        }
        .catch { emit(Theme.SYSTEM) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Theme.SYSTEM,
        )

    // Language state from domain layer with proper error handling
    val languageState: StateFlow<Language> = observeLanguageUseCase()
        .map { result ->
            when (result) {
                is Result.Success -> result.data
                is Result.Error -> Language.ENGLISH
            }
        }
        .catch { emit(Language.ENGLISH) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Language.ENGLISH,
        )

    // Navigation events flow
    private val _navigationEvents = MutableSharedFlow<NavigationActions>()
    val navigationEvents: SharedFlow<NavigationActions> = _navigationEvents.asSharedFlow()

    // UI events flow for app-level UI events (dialogs, etc.)
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
