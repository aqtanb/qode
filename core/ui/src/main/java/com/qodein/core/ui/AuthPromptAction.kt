package com.qodein.core.ui

import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeinIcons

/**
 * Authentication prompt actions that trigger contextual auth prompts
 */
enum class AuthPromptAction(val titleResId: Int, val messageResId: Int, val iconVector: ImageVector) {
    SubmitPromocode(
        titleResId = R.string.auth_submit_promo_title,
        messageResId = R.string.auth_submit_promo_message,
        iconVector = QodeActionIcons.Add,
    ),
    UpvotePrompt(
        titleResId = R.string.auth_upvote_title,
        messageResId = R.string.auth_upvote_message,
        iconVector = QodeActionIcons.Thumbs,
    ),
    DownvotePrompt(
        titleResId = R.string.auth_downvote_title,
        messageResId = R.string.auth_downvote_message,
        iconVector = QodeActionIcons.ThumbsDown,
    ),
    WriteComment(
        titleResId = R.string.auth_comment_title,
        messageResId = R.string.auth_comment_message,
        iconVector = QodeActionIcons.Comment,
    ),
    BookmarkPromoCode(
        titleResId = R.string.auth_bookmark_promo_title,
        messageResId = R.string.auth_bookmark_promo_message,
        iconVector = QodeActionIcons.Bookmark,
    ),
    FollowStore(
        titleResId = R.string.auth_follow_store_title,
        messageResId = R.string.auth_follow_store_message,
        iconVector = QodeActionIcons.Follow,
    ),
    CreatePost(
        titleResId = R.string.auth_create_post_title,
        messageResId = R.string.auth_create_post_message,
        iconVector = QodeinIcons.PostAdd,
    )
}
