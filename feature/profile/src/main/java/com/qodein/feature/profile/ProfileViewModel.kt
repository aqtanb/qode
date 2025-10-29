package com.qodein.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logLogout
import com.qodein.shared.common.Result
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
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
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val analyticsHelper: AnalyticsHelper
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

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.SignOutClicked -> signOut()
            is ProfileAction.RetryClicked -> checkAuthState()
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

        authJob = getAuthStateUseCase()
            .onEach { authUser ->
                if (authUser != null) {
                    // Fetch user with real stats from Firestore
                    getUserByIdUseCase(authUser.id.value)
                        .onEach { result ->
                            _state.value = when (result) {
                                is Result.Success -> ProfileUiState.Success(user = result.data)
                                is Result.Error -> {
                                    // Fallback to auth user if Firestore fetch fails
                                    ProfileUiState.Success(user = authUser)
                                }
                            }
                        }
                        .launchIn(viewModelScope)
                } else {
                    emitEvent(ProfileEvent.NavigateToAuth)
                }
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
                when (result) {
                    is Result.Success -> {
                        // Log successful logout
                        analyticsHelper.logLogout()
                        // Only navigate after successful sign out
                        emitEvent(ProfileEvent.SignedOut)
                    }
                    is Result.Error -> {
                        // Restart auth monitoring if sign out fails
                        checkAuthState()
                        _state.value = ProfileUiState.Error(
                            errorType = result.error,
                            isRetryable = true,
                            shouldShowSnackbar = false,
                            errorCode = null,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}
