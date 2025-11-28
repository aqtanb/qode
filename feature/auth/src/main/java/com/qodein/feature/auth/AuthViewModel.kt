package com.qodein.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logLogin
import com.qodein.core.ui.auth.IdTokenProvider
import com.qodein.shared.common.Result
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.legal.GetLegalDocumentUseCase
import com.qodein.shared.model.DocumentType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sign-in screen ViewModel - handles UI-specific logic and side effects.
 *
 * Uses AuthStateManager for authentication business logic
 * and handles screen-specific concerns like analytics and navigation events.
 */
class AuthViewModel(
    private val idTokenProvider: IdTokenProvider,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val getLegalDocumentUseCase: GetLegalDocumentUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState())
    val authState = _authState.asStateFlow()

    private val _legalDocumentState = MutableStateFlow<LegalDocumentUiState>(LegalDocumentUiState.Closed)
    val legalDocumentState = _legalDocumentState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    fun handleAction(action: AuthAction) {
        when (action) {
            is AuthAction.AuthWithGoogleClicked -> signInWithGoogle(action.activityContext)
            is AuthAction.AuthErrorDismissed -> dismissError()

            is AuthAction.LegalDocumentClicked -> getLegalDocument(type = action.documentType)
            is AuthAction.LegalDocumentRetryClicked -> getLegalDocument(type = action.documentType)
            AuthAction.LegalDocumentDismissed -> {
                _legalDocumentState.value = LegalDocumentUiState.Closed
            }
        }
    }

    private fun signInWithGoogle(context: Context) {
        _authState.update { it.copy(isSigningIn = true, error = null) }

        viewModelScope.launch {
            when (val tokenResult = idTokenProvider.getIdToken(context)) {
                is Result.Error -> {
                    analyticsHelper.logLogin(method = "google", success = false)
                    _authState.update { it.copy(isSigningIn = false, error = tokenResult.error) }
                }
                is Result.Success -> {
                    when (val signInResult = signInWithGoogleUseCase(tokenResult.data)) {
                        is Result.Success -> {
                            analyticsHelper.logLogin(method = "google", success = true)
                            _authState.update { it.copy(isSigningIn = false) }
                            emitEvent(AuthEvent.SignedIn)
                        }
                        is Result.Error -> {
                            analyticsHelper.logLogin(method = "google", success = false)
                            _authState.update { it.copy(isSigningIn = false, error = signInResult.error) }
                        }
                    }
                }
            }
        }
    }

    private fun getLegalDocument(type: DocumentType) {
        viewModelScope.launch {
            _legalDocumentState.value = LegalDocumentUiState.Loading(documentType = type)
            val result = getLegalDocumentUseCase(
                type = type,
            )

            when (result) {
                is Result.Error -> {
                    _legalDocumentState.value = LegalDocumentUiState.Error(
                        documentType = type,
                        errorType = result.error,
                    )
                }
                is Result.Success -> {
                    _legalDocumentState.value = LegalDocumentUiState.Content(document = result.data)
                }
            }
        }
    }

    private fun emitEvent(event: AuthEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun dismissError() {
        _authState.update { it.copy(error = null) }
    }
}
