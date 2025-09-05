package com.qodein.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logLogin
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.getErrorCode
import com.qodein.shared.common.result.isRetryable
import com.qodein.shared.common.result.shouldShowSnackbar
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
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
class AuthViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    private var signInJob: Job? = null
    private var authStateJob: Job? = null

    init {
        // Initialize with current auth state
        checkAuthState()
    }

    fun handleAction(action: AuthAction) {
        when (action) {
            is AuthAction.SignInWithGoogleClicked -> signInWithGoogle()
            is AuthAction.RetryClicked -> signInWithGoogle()
            is AuthAction.DismissErrorClicked -> clearError()
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
                _state.value = when (result) {
                    is Result.Loading -> AuthUiState.Loading
                    is Result.Success -> {
                        analyticsHelper.logLogin(method = "google", success = true)
                        emitEvent(AuthEvent.SignedIn)
                        AuthUiState.Success(user = result.data)
                    }
                    is Result.Error -> {
                        analyticsHelper.logLogin(method = "google", success = false)
                        AuthUiState.Error(
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

    private fun emitEvent(event: AuthEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun checkAuthState() {
        // Cancel any existing auth state monitoring
        authStateJob?.cancel()

        authStateJob = getAuthStateUseCase()
            .onEach { result ->
                _state.value = when (result) {
                    is Result.Loading -> AuthUiState.Loading
                    is Result.Success -> {
                        when (val authState = result.data) {
                            is AuthState.Loading -> AuthUiState.Loading
                            is AuthState.Unauthenticated -> AuthUiState.Idle
                            is AuthState.Authenticated -> AuthUiState.Success(user = authState.user)
                        }
                    }
                    is Result.Error -> {
                        // If we can't determine auth state, assume not authenticated
                        AuthUiState.Idle
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun clearError() {
        _state.value = AuthUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        signInJob?.cancel()
        authStateJob?.cancel()
    }
}
