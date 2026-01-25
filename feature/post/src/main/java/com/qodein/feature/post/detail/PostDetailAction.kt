package com.qodein.feature.post.detail

import com.qodein.core.ui.AuthPromptAction
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState

sealed interface PostDetailAction {
    data class ToggleVoteClicked(val voteState: VoteState) : PostDetailAction
    data class BlockUserClicked(val userId: UserId) : PostDetailAction
    data class ReportPostClicked(val postId: String) : PostDetailAction
}

sealed interface PostDetailEvent {
    data class ShowError(val error: OperationError) : PostDetailEvent
    data class NavigateToAuth(val action: AuthPromptAction) : PostDetailEvent
    data class NavigateToBlockUser(val userId: UserId, val username: String, val photoUrl: String?) : PostDetailEvent
    data class NavigateToReport(val reportedItemId: String, val itemTitle: String, val itemAuthor: String) : PostDetailEvent
}
