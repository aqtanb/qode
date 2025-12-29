package com.qodein.feature.auth

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logLogin
import com.qodein.core.ui.auth.IdTokenProvider
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import com.qodein.shared.domain.usecase.legal.GetLegalDocumentUseCase
import com.qodein.shared.domain.usecase.user.AcceptConsentAndCreateUserUseCase
import com.qodein.shared.model.DocumentType
import com.qodein.shared.model.GoogleAuthResult
import com.qodein.shared.model.UserResolutionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthBottomSheetUiState {
    data object SignIn : AuthBottomSheetUiState
    data class ConsentRequired(val authUser: GoogleAuthResult) : AuthBottomSheetUiState
}

class AuthBottomSheetViewModel(
    internal val savedStateHandle: SavedStateHandle,
    private val idTokenProvider: IdTokenProvider,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val acceptConsentUseCase: AcceptConsentAndCreateUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getLegalDocumentUseCase: GetLegalDocumentUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthBottomSheetUiState>(AuthBottomSheetUiState.SignIn)
    val uiState = _uiState.asStateFlow()

    private val _legalDocumentState = MutableStateFlow<LegalDocumentUiState>(LegalDocumentUiState.Closed)
    val legalDocumentState = _legalDocumentState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<OperationError?>(null)
    val error = _error.asStateFlow()

    fun signInWithGoogle(context: Context) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            when (val tokenResult = idTokenProvider.getIdToken(context)) {
                is Result.Error -> {
                    analyticsHelper.logLogin(method = "google", success = false)
                    _isLoading.value = false
                    _error.value = tokenResult.error
                }
                is Result.Success -> {
                    handleSignInResult(tokenResult.data)
                }
            }
        }
    }

    private suspend fun handleSignInResult(idToken: String) {
        when (val result = signInWithGoogleUseCase(idToken)) {
            is Result.Error -> {
                analyticsHelper.logLogin(method = "google", success = false)
                _isLoading.value = false
                _error.value = result.error
            }
            is Result.Success -> {
                when (val resolutionResult = result.data) {
                    is UserResolutionResult.ExistingUser -> {
                        analyticsHelper.logLogin(method = "google", success = true)
                        _isLoading.value = false
                        setAuthSuccess()
                    }
                    is UserResolutionResult.NewUserNeedsConsent -> {
                        _isLoading.value = false
                        _uiState.value = AuthBottomSheetUiState.ConsentRequired(resolutionResult.authUser)
                    }
                }
            }
        }
    }

    fun acceptConsent(authUser: GoogleAuthResult) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            when (val result = acceptConsentUseCase(authUser)) {
                is Result.Success -> {
                    analyticsHelper.logLogin(method = "google", success = true)
                    _isLoading.value = false
                    setAuthSuccess()
                }
                is Result.Error -> {
                    _isLoading.value = false
                    _error.value = result.error
                }
            }
        }
    }

    fun declineConsent() {
        viewModelScope.launch {
            signOutUseCase()
            setAuthCancelled()
        }
    }

    fun getLegalDocument(type: DocumentType) {
        viewModelScope.launch {
            _legalDocumentState.value = LegalDocumentUiState.Loading(documentType = type)
            when (val result = getLegalDocumentUseCase(type)) {
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

    fun dismissLegalDocument() {
        _legalDocumentState.value = LegalDocumentUiState.Closed
    }

    fun dismissError() {
        _error.value = null
    }

    private fun setAuthSuccess() {
        savedStateHandle[AUTH_RESULT_KEY] = AUTH_RESULT_SUCCESS
    }

    private fun setAuthCancelled() {
        savedStateHandle[AUTH_RESULT_KEY] = AUTH_RESULT_CANCELLED
    }

    companion object {
        const val AUTH_RESULT_KEY = "auth_result"
        const val AUTH_RESULT_SUCCESS = "success"
        const val AUTH_RESULT_CANCELLED = "cancelled"
    }
}
