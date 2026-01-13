package com.qodein.feature.block.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.usecase.user.GetBlockedUserIdsUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.domain.usecase.user.UnblockUserUseCase
import com.qodein.shared.model.User
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BlockedUsersViewModel(
    private val getBlockedUserIdsUseCase: GetBlockedUserIdsUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val unblockUserUseCase: UnblockUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockedUsersUiState>(BlockedUsersUiState.Loading)
    val uiState: StateFlow<BlockedUsersUiState> = _uiState.asStateFlow()

    init {
        observeBlockedUsers()
    }

    fun onAction(action: BlockedUsersAction) {
        when (action) {
            BlockedUsersAction.RetryLoadingUsers -> observeBlockedUsers()

            is BlockedUsersAction.ShowConfirmationDialog -> openConfirmationDialog(action.user)
            BlockedUsersAction.ConfirmUnblock -> handleUnblock()
            BlockedUsersAction.DismissConfirmationDialog -> closeConfirmationDialog()
        }
    }

    private fun observeBlockedUsers() {
        viewModelScope.launch {
            // Only show the full-screen shimmer if we don't have data yet.
            // If we already have a Success state, we stay in it to avoid flickering.
            if (_uiState.value !is BlockedUsersUiState.Success) {
                _uiState.value = BlockedUsersUiState.Loading
            }

            // TODO: Add pagination to avoid fetching too many users at once
            // TODO: Add blocked users limit to prevent abuse (e.g., Firestore read limits)
            getBlockedUserIdsUseCase().collectLatest { blockedUserIds ->
                try {
                    // Use coroutineScope to ensure parallel work is cancelled if the flow restarts
                    val users = coroutineScope {
                        blockedUserIds.map { userId ->
                            async { getUserByIdUseCase(userId) }
                        }.awaitAll().mapNotNull { result ->
                            (result as? Result.Success)?.data
                        }
                    }

                    _uiState.update { currentState ->
                        val currentDialog = (currentState as? BlockedUsersUiState.Success)?.dialogState
                            ?: UnblockDialogState.Hidden

                        BlockedUsersUiState.Success(users, currentDialog)
                    }
                } catch (e: Exception) {
                    Logger.e(e) { "Failed to fetch blocked users" }
                    _uiState.value = BlockedUsersUiState.Error(SystemError.Unknown)
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
            // 1. Move the DIALOG state to Loading immediately
            _uiState.update { state ->
                if (state is BlockedUsersUiState.Success) {
                    state.copy(dialogState = UnblockDialogState.Loading(user))
                } else {
                    state
                }
            }

            // 2. Perform the unblock
            when (val result = unblockUserUseCase(user.id.value)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        if (state is BlockedUsersUiState.Success) {
                            state.copy(dialogState = UnblockDialogState.Hidden)
                        } else {
                            state
                        }
                    }
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
