package com.qodein.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.domain.usecase.auth.SignInWithGoogleUseCase
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
class AuthViewModel @Inject constructor(private val signInWithGoogleUseCase: SignInWithGoogleUseCase) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    private var signInJob: Job? = null

    fun handleAction(action: AuthAction) {
        when (action) {
            is AuthAction.SignInWithGoogleClicked -> signInWithGoogle()
            is AuthAction.RetryClicked -> signInWithGoogle()
            is AuthAction.TermsOfServiceClicked -> emitEvent(AuthEvent.TermsOfServiceRequested)
            is AuthAction.PrivacyPolicyClicked -> emitEvent(AuthEvent.PrivacyPolicyRequested)
        }
    }

    private fun signInWithGoogle() {
        // Cancel any existing sign in operation
        signInJob?.cancel()

        _state.value = AuthUiState.Loading

        signInJob = signInWithGoogleUseCase()
            .onEach { result ->
                _state.value = result.fold(
                    onSuccess = { user ->
                        val successState = AuthUiState.Success(user = user)
                        emitEvent(AuthEvent.SignedIn)
                        successState
                    },
                    onFailure = { exception ->
                        val isRetryable = exception !is SecurityException
                        AuthUiState.Error(exception, isRetryable)
                    },
                )
            }
            .launchIn(viewModelScope)
    }

    private fun emitEvent(event: AuthEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    fun clearError() {
        _state.value = AuthUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        signInJob?.cancel()
    }
}
