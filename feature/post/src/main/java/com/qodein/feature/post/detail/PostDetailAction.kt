package com.qodein.feature.post.detail

import android.content.Context
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState

sealed class PostDetailAction {
    data class UpvoteClicked(val postId: String, val currentVoteState: VoteState, val userId: UserId?) : PostDetailAction()
    data class DownvoteClicked(val postId: String, val currentVoteState: VoteState, val userId: UserId?) : PostDetailAction()
    data class SignInWithGoogleClicked(val context: Context) : PostDetailAction()
}
