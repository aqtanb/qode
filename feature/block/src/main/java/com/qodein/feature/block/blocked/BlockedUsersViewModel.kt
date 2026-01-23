package com.qodein.feature.block.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.shared.common.Result
import com.qodein.shared.domain.usecase.user.GetBlockedUsersUseCase
import com.qodein.shared.domain.usecase.user.UnblockUserUseCase
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BlockedUsersViewModel(
    private val getBlockedUsersUseCase: GetBlockedUsersUseCase,
    private val unblockUserUseCase: UnblockUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockedUsersUiState>(BlockedUsersUiState.Loading)
    val uiState: StateFlow<BlockedUsersUiState> = _uiState.asStateFlow()

    init {
        loadBlockedUsers()
    }

    fun onAction(action: BlockedUsersAction) {
        when (action) {
            BlockedUsersAction.RetryLoadingUsers -> loadBlockedUsers()
            BlockedUsersAction.LoadMoreUsers -> loadMoreBlockedUsers()

            is BlockedUsersAction.ShowConfirmationDialog -> openConfirmationDialog(action.user)
            BlockedUsersAction.ConfirmUnblock -> handleUnblock()
            BlockedUsersAction.DismissConfirmationDialog -> closeConfirmationDialog()
        }
    }

    private fun loadBlockedUsers() {
        viewModelScope.launch {
            _uiState.update { BlockedUsersUiState.Loading }

            val result = getBlockedUsersUseCase(cursor = null)

            _uiState.update { currentState ->
                when (result) {
                    is Result.Success -> BlockedUsersUiState.Success(
                        blockedUsers = result.data.data,
                        hasMore = result.data.hasMore,
                        nextCursor = result.data.nextCursor,
                        dialogState = UnblockDialogState.Hidden,
                    )
                    is Result.Error -> BlockedUsersUiState.Error(result.error)
                }
            }
        }
    }

    private fun loadMoreBlockedUsers() {
        val currentState = _uiState.value as? BlockedUsersUiState.Success ?: return

        if (!currentState.hasMore || currentState.isLoadingMore) return

        _uiState.update {
            it as BlockedUsersUiState.Success
            it.copy(isLoadingMore = true)
        }

        viewModelScope.launch {
            when (val result = getBlockedUsersUseCase(cursor = currentState.nextCursor)) {
                is Result.Error -> _uiState.update {
                    (it as BlockedUsersUiState.Success).copy(isLoadingMore = false)
                }
                is Result.Success -> _uiState.update {
                    (it as BlockedUsersUiState.Success).copy(
                        blockedUsers = currentState.blockedUsers + result.data.data,
                        hasMore = result.data.hasMore,
                        nextCursor = result.data.nextCursor,
                        isLoadingMore = false,
                    )
                }
            }
        }
    }

    private fun openConfirmationDialog(user: User) {
        _uiState.update { state ->
            if (state is BlockedUsersUiState.Success) {
                state.copy(dialogState = UnblockDialogState.Idle(user))
            } else {
                state
            }
        }
    }

    private fun handleUnblock() {
        val currentState = _uiState.value as? BlockedUsersUiState.Success ?: return
        val dialog = currentState.dialogState as? UnblockDialogState.Visible ?: return
        val user = dialog.user

        viewModelScope.launch {
            _uiState.update { state ->
                if (state is BlockedUsersUiState.Success) {
                    state.copy(dialogState = UnblockDialogState.Loading(user))
                } else {
                    state
                }
            }

            when (val result = unblockUserUseCase(user.id.value)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        if (state is BlockedUsersUiState.Success) {
                            state.copy(dialogState = UnblockDialogState.Hidden)
                        } else {
                            state
                        }
                    }
                    loadBlockedUsers()
                }
                is Result.Error -> {
                    _uiState.update { state ->
                        if (state is BlockedUsersUiState.Success) {
                            state.copy(dialogState = UnblockDialogState.Error(user, result.error))
                        } else {
                            state
                        }
                    }
                }
            }
        }
    }
    private fun closeConfirmationDialog() {
        _uiState.update { state ->
            if (state is BlockedUsersUiState.Success) {
                state.copy(dialogState = UnblockDialogState.Hidden)
            } else {
                state
            }
        }
    }
}
