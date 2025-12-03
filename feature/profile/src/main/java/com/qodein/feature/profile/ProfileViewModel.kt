package com.qodein.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logLogout
import com.qodein.shared.common.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import com.qodein.shared.domain.usecase.user.ObserveUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeUserUseCase: ObserveUserUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    init {
        observeAuthState()
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.SignOutClicked -> signOut()
            is ProfileAction.RetryClicked -> observeAuthState()
        }
    }

    private fun emitEvent(event: ProfileEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getAuthStateUseCase()
                .collectLatest { authState ->
                    when (authState) {
                        is AuthState.Authenticated -> {
                            observeUserUseCase(authState.userId.value)
                                .collectLatest { userResult ->
                                    when (userResult) {
                                        is Result.Success -> _uiState.value = ProfileUiState.Success(userResult.data)
                                        is Result.Error -> _uiState.value = ProfileUiState.Error(userResult.error)
                                    }
                                }
                        }

                        AuthState.Unauthenticated -> {
                            _uiState.value = ProfileUiState.Loading
                            emitEvent(ProfileEvent.NavigateToAuth)
                        }
                    }
                }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            analyticsHelper.logLogout()
            emitEvent(ProfileEvent.SignedOut)
        }
    }
}
