package com.qodein.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.domain.usecase.auth.GetCurrentUserUseCase
import com.qodein.core.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state = _state.asStateFlow()

    init {
        checkAuthState()
    }

    fun handleAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.SignOutClicked -> signOut()
            is ProfileAction.RetryClicked -> checkAuthState()
        }
    }

    private fun checkAuthState() {
        _state.value = ProfileUiState.Loading

        getCurrentUserUseCase()
            .onEach { result ->
                _state.value = result.fold(
                    onSuccess = { user ->
                        if (user != null) {
                            ProfileUiState.SignedIn(user = user)
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
        signOutUseCase()
            .onEach { result ->
                result.fold(
                    onSuccess = {
                        // Sign out successful - navigation will handle redirect to auth/home
                        // Keep current state as the screen will be navigated away immediately
                    },
                    onFailure = { exception ->
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
