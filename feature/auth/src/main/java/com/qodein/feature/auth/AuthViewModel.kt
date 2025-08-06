package com.qodein.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.domain.usecase.auth.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val signInWithGoogleUseCase: SignInWithGoogleUseCase) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state = _state.asStateFlow()

    fun handleAction(action: AuthAction) {
        when (action) {
            is AuthAction.SignInWithGoogleClicked -> signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        _state.value = AuthUiState.Loading

        signInWithGoogleUseCase()
            .onEach { result ->
                _state.value = result.fold(
                    onSuccess = { user ->
                        AuthUiState.Success(
                            user = user,
                        )
                    },
                    onFailure = { exception ->
                        val errorMessage = when (exception) {
                            is SecurityException -> "Sign-in was cancelled or rejected"
                            is java.io.IOException -> "Network error. Please check your connection"
                            is IllegalStateException -> "Google Play Services unavailable"
                            else -> "Failed to sign in with Google"
                        }

                        val isRetryable = exception !is SecurityException
                        AuthUiState.Error(errorMessage, isRetryable)
                    },
                )
            }
            .launchIn(viewModelScope)
    }

    fun clearError() {
        _state.value = AuthUiState.Idle
    }
}
