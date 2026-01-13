package com.qodein.feature.block.blocked

import com.qodein.shared.model.User

sealed interface BlockedUsersAction {
    data object RetryLoadingUsers : BlockedUsersAction

    data class ShowConfirmationDialog(val user: User) : BlockedUsersAction
    data object DismissConfirmationDialog : BlockedUsersAction
    data object ConfirmUnblock : BlockedUsersAction
}
