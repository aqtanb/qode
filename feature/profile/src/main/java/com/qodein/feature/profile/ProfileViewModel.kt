package com.qodein.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.shared.common.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesByUserUseCase
import com.qodein.shared.domain.usecase.user.ObserveUserUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val observeUserUseCase: ObserveUserUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getPromocodesByUserUseCase: GetPromocodesByUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    init {
        observeAuthState()
        getPromocodes()
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.SignOutClicked -> signOut()
            ProfileAction.RetryClicked -> observeAuthState()
            ProfileAction.BlockedClicked -> {
                emitEvent(ProfileEvent.NavigateToBlockedUsers)
            }

            ProfileAction.LoadMorePosts -> TODO()
            ProfileAction.LoadMorePromocodes -> TODO()
            is ProfileAction.PostClicked -> TODO()
            is ProfileAction.PromocodeClicked -> TODO()
            ProfileAction.RetryPostsClicked -> TODO()
            ProfileAction.RetryPromocodesClicked -> TODO()
            is ProfileAction.TabSelected -> {
                val currentState = _uiState.value as? ProfileUiState.Success ?: return
                _uiState.update { currentState.copy(selectedTab = action.tab) }
            }
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

    private fun getPromocodes() {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return
        _uiState.update { currentState.copy(promocodesState = PaginatedDataState.Loading) }
        viewModelScope.launch {
            when (val result = getPromocodesByUserUseCase(userId = currentState.user.id)) {
                is Result.Error -> _uiState.update { currentState.copy(promocodesState = PaginatedDataState.Error(result.error)) }
                is Result.Success -> _uiState.update {
                    currentState.copy(
                        promocodesState = PaginatedDataState.Success(
                            items = result.data.data,
                            hasMore = result.data.hasMore,
                            nextCursor = result.data.nextCursor,
                        ),
                    )
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            emitEvent(ProfileEvent.SignedOut)
        }
    }
}
