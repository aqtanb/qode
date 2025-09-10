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
import com.qodein.shared.domain.auth.AuthStateManager
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sign-in screen ViewModel - handles UI-specific logic and side effects.
 *
 * Uses AuthStateManager for authentication business logic
 * and handles screen-specific concerns like analytics and navigation events.
 */
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val authStateManager: AuthStateManager,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _state = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    fun handleAction(action: SignInAction) {
        when (action) {
            is SignInAction.SignInWithGoogleClicked -> {
                signInWithGoogle()
            }
            is SignInAction.RetryClicked -> {
                signInWithGoogle()
            }
            is SignInAction.DismissErrorClicked -> clearError()
            is SignInAction.TermsOfServiceClicked -> emitEvent(AuthEvent.TermsOfServiceRequested)
            is SignInAction.PrivacyPolicyClicked -> emitEvent(AuthEvent.PrivacyPolicyRequested)
        }
    }

    private fun signInWithGoogle() {
        _state.value = SignInUiState.Loading

        signInWithGoogleUseCase()
            .onEach { result ->
                _state.value = when (result) {
                    is Result.Loading -> SignInUiState.Loading
                    is Result.Success -> {
                        analyticsHelper.logLogin(method = "google", success = true)
                        emitEvent(AuthEvent.SignedIn)
                        SignInUiState.Idle
                    }
                    is Result.Error -> {
                        analyticsHelper.logLogin(method = "google", success = false)
                        SignInUiState.Error(
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

    private fun clearError() {
        _state.value = SignInUiState.Idle
    }
}
