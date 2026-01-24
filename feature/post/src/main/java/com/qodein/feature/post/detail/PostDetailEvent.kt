package com.qodein.feature.post.detail

import com.qodein.core.ui.AuthPromptAction
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.UserId

sealed class PostDetailEvent {
    data class ShowError(val error: OperationError) : PostDetailEvent()
    data class NavigateToAuth(val action: AuthPromptAction) : PostDetailEvent()
    data class NavigateToBlockUser(val userId: UserId, val username: String, val photoUrl: String?) : PostDetailEvent()
    data class NavigateToReport(val reportedItemId: String, val itemTitle: String, val itemAuthor: String) : PostDetailEvent()
}
