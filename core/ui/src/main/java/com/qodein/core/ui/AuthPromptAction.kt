package com.qodein.core.ui

import androidx.annotation.Keep
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.icon.PostIcons
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
    CreatePost(
        titleResId = R.string.auth_create_post_title,
        messageResId = R.string.auth_create_post_message,
        iconVector = PostIcons.PostAdd,
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
    ),
    Vote(
        titleResId = R.string.ui_auth_vote_title,
        messageResId = R.string.ui_auth_vote_message,
        iconVector = UIIcons.VoteScore,
    ),
    Block(
        titleResId = R.string.ui_auth_block_title,
        messageResId = R.string.ui_auth_block_message,
        iconVector = UIIcons.Block,
    )
}
