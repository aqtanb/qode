package com.qodein.feature.post.detail

import com.qodein.core.ui.AuthPromptAction
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.PostId
import com.qodein.shared.model.ShareContent
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState

sealed interface PostDetailAction {
    data class ToggleVoteClicked(val voteState: VoteState) : PostDetailAction
    data object BlockUserClicked : PostDetailAction
    data object ReportPostClicked : PostDetailAction
    data object SharePostClicked : PostDetailAction
}

sealed interface PostDetailEvent {
    data class ShowError(val error: OperationError) : PostDetailEvent
    data class NavigateToAuth(val action: AuthPromptAction) : PostDetailEvent
    data class NavigateToBlockUser(val userId: UserId, val username: String, val photoUrl: String?) : PostDetailEvent
    data class NavigateToReport(val reportedItemId: PostId, val itemTitle: String, val itemAuthor: String) : PostDetailEvent
    data class SharePost(val shareContent: ShareContent) : PostDetailEvent
}
