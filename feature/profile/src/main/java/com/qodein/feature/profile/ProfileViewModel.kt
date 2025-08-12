package com.qodein.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.domain.usecase.auth.GetCurrentUserUseCase
import com.qodein.core.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase
    // TODO: Add GetUserStatsUseCase for promocodes, upvotes, downvotes
    // TODO: Add GetUserAchievementsUseCase for achievements data
    // TODO: Add GetUserActivityUseCase for user journey (promocodes & comments history)
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    private var authJob: Job? = null

    init {
        checkAuthState()
    }

    fun handleAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.SignOutClicked -> signOut()
            is ProfileAction.RetryClicked -> checkAuthState()
            is ProfileAction.EditProfileClicked -> emitEvent(ProfileEvent.NavigateToEditProfile)
            is ProfileAction.AchievementsClicked -> emitEvent(ProfileEvent.NavigateToAchievements)
            is ProfileAction.UserJourneyClicked -> emitEvent(ProfileEvent.NavigateToUserJourney)
        }
    }

    private fun emitEvent(event: ProfileEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun checkAuthState() {
        // Cancel any existing auth job
        authJob?.cancel()

        _state.value = ProfileUiState.Loading

        authJob = getCurrentUserUseCase()
            .onEach { result ->
                _state.value = result.fold(
                    onSuccess = { user ->
                        if (user != null) {
                            ProfileUiState.Success(user = user)
                        } else {
                            // With smart routing, this should not happen
                            // If user is null, it means auth state is inconsistent
                            ProfileUiState.Error(
                                exception = IllegalStateException("User not authenticated - navigation should have redirected to auth"),
                                isRetryable = true,
                            )
                        }
                    },
                    onFailure = { exception ->
                        ProfileUiState.Error(
                            exception = exception,
                            isRetryable = true,
                        )
                    },
                )
            }
            .launchIn(viewModelScope)
    }

    private fun signOut() {
        // Cancel auth state monitoring during sign out
        authJob?.cancel()

        // Show loading state during sign out
        _state.value = ProfileUiState.Loading

        // Perform sign out and navigate only on success
        signOutUseCase()
            .onEach { result ->
                result.fold(
                    onSuccess = {
                        // Only navigate after successful sign out
                        emitEvent(ProfileEvent.NavigateToSignOut)
                    },
                    onFailure = { exception ->
                        // Restart auth monitoring if sign out fails
                        checkAuthState()
                        _state.value = ProfileUiState.Error(
                            exception = exception,
                            isRetryable = true,
                        )
                    },
                )
            }
            .launchIn(viewModelScope)
    }
}
