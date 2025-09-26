package com.qodein.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logLogout
import com.qodein.shared.common.Result
import com.qodein.shared.common.result.getErrorCode
import com.qodein.shared.common.result.isRetryable
import com.qodein.shared.common.result.shouldShowSnackbar
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
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
    private val signOutUseCase: SignOutUseCase,
    private val analyticsHelper: AnalyticsHelper
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
            is ProfileAction.EditProfileClicked -> {
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "profile_action",
                        extras = listOf(
                            AnalyticsEvent.Param("action", "edit_profile"),
                        ),
                    ),
                )
                emitEvent(ProfileEvent.EditProfileRequested)
            }
            is ProfileAction.AchievementsClicked -> {
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "profile_action",
                        extras = listOf(
                            AnalyticsEvent.Param("action", "view_achievements"),
                        ),
                    ),
                )
                emitEvent(ProfileEvent.AchievementsRequested)
            }
            is ProfileAction.UserJourneyClicked -> {
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "profile_action",
                        extras = listOf(
                            AnalyticsEvent.Param("action", "view_user_journey"),
                        ),
                    ),
                )
                emitEvent(ProfileEvent.UserJourneyRequested)
            }
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
            .onEach { result ->
                _state.value = when (result) {
                    is Result.Loading -> ProfileUiState.Loading
                    is Result.Success -> {
                        val authState = result.data
                        when (authState) {
                            is AuthState.Loading -> ProfileUiState.Loading
                            is AuthState.Authenticated -> ProfileUiState.Success(user = authState.user)
                            is AuthState.Unauthenticated -> {
                                // With smart routing, this should not happen
                                // If user is unauthenticated, navigation should have redirected to auth
                                val exception = IllegalStateException("User not authenticated - navigation should have redirected to auth")
                                ProfileUiState.Error(
                                    errorType = exception.toErrorType(),
                                    isRetryable = exception.isRetryable(),
                                    shouldShowSnackbar = exception.shouldShowSnackbar(),
                                    errorCode = exception.getErrorCode(),
                                )
                            }
                        }
                    }
                    is Result.Error -> {
                        ProfileUiState.Error(
                            errorType = result.exception.toErrorType(),
                            isRetryable = result.exception.isRetryable(),
                            shouldShowSnackbar = result.exception.shouldShowSnackbar(),
                            errorCode = result.exception.getErrorCode(),
                        )
                    }
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
                    is Result.Loading -> { /* Loading already shown above */ }
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
                            errorType = result.exception.toErrorType(),
                            isRetryable = result.exception.isRetryable(),
                            shouldShowSnackbar = result.exception.shouldShowSnackbar(),
                            errorCode = result.exception.getErrorCode(),
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}
