package com.qodein.core.ui

import androidx.annotation.Keep
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.icon.QodeinIcons
import com.qodein.core.designsystem.icon.UIIcons

/**
 * Authentication prompt actions that trigger contextual auth prompts
 */
@Keep
enum class AuthPromptAction(val titleResId: Int, val messageResId: Int, val iconVector: ImageVector) {
    SubmitPromocode(
        titleResId = R.string.auth_submit_promo_title,
        messageResId = R.string.auth_submit_promo_message,
        iconVector = ActionIcons.Add,
    ),
    UpvotePrompt(
        titleResId = R.string.auth_upvote_title,
        messageResId = R.string.auth_upvote_message,
        iconVector = ActionIcons.Thumbs,
    ),
    DownvotePrompt(
        titleResId = R.string.auth_downvote_title,
        messageResId = R.string.auth_downvote_message,
        iconVector = ActionIcons.ThumbsDown,
    ),
    WriteComment(
        titleResId = R.string.auth_comment_title,
        messageResId = R.string.auth_comment_message,
        iconVector = ActionIcons.Comment,
    ),
    BookmarkPromoCode(
        titleResId = R.string.auth_bookmark_promo_title,
        messageResId = R.string.auth_bookmark_promo_message,
        iconVector = ActionIcons.Bookmark,
    ),
    FollowStore(
        titleResId = R.string.auth_follow_store_title,
        messageResId = R.string.auth_follow_store_message,
        iconVector = ActionIcons.Follow,
    ),
    CreatePost(
        titleResId = R.string.auth_create_post_title,
        messageResId = R.string.auth_create_post_message,
        iconVector = QodeinIcons.PostAdd,
    ),
    Profile(
        titleResId = R.string.auth_profile_title,
        messageResId = R.string.auth_profile_message,
        iconVector = UIIcons.AccountCircle,
    ),
    ReportContent(
        titleResId = R.string.auth_report_title,
        messageResId = R.string.auth_report_message,
        iconVector = UIIcons.Report,
    )
}
