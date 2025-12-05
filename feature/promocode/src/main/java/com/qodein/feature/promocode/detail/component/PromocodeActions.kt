package com.qodein.feature.promocode.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeinFilterChip
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.feature.promocode.R
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.VoteState

@Composable
fun PromocodeActions(
    promocode: Promocode,
    upvoteCount: Int,
    downvoteCount: Int,
    isUpvotedByCurrentUser: Boolean,
    isDownvotedByCurrentUser: Boolean,
    onUpvoteClicked: () -> Unit,
    onDownvoteClicked: () -> Unit,
    onShareClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val upvoteContentDescription = stringResource(R.string.cd_upvote, upvoteCount)
    val downvoteContentDescription = stringResource(R.string.cd_downvote, downvoteCount)
    val shareContentDescription = stringResource(R.string.cd_share)
    val userVoteState = when {
        isUpvotedByCurrentUser -> VoteState.UPVOTE
        isDownvotedByCurrentUser -> VoteState.DOWNVOTE
        else -> VoteState.NONE
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QodeinFilterChip(
            label = upvoteCount.toString(),
            onClick = onUpvoteClicked,
            selected = userVoteState == VoteState.UPVOTE,
            leadingIcon = {
                Icon(
                    imageVector = QodeActionIcons.Up,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            },
            modifier = Modifier.semantics {
                contentDescription = upvoteContentDescription
            },
        )

        QodeinFilterChip(
            label = downvoteCount.toString(),
            onClick = onDownvoteClicked,
            selected = userVoteState == VoteState.DOWNVOTE,
            leadingIcon = {
                Icon(
                    imageVector = QodeActionIcons.Down,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            },
            modifier = Modifier.semantics {
                contentDescription = downvoteContentDescription
            },
        )

        Spacer(modifier = Modifier.weight(1f))

        QodeinFilterChip(
            label = stringResource(id = R.string.action_share),
            onClick = onShareClicked,
            selected = false,
            leadingIcon = {
                Icon(
                    imageVector = QodeActionIcons.Share,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            },
            modifier = Modifier.semantics {
                contentDescription = shareContentDescription
            },
        )
    }
}

@ThemePreviews
@Composable
private fun PromocodeActionsPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode

        PromocodeActions(
            promocode = samplePromoCode,
            upvoteCount = samplePromoCode.upvotes,
            downvoteCount = samplePromoCode.downvotes,
            isUpvotedByCurrentUser = true,
            isDownvotedByCurrentUser = false,
            onUpvoteClicked = {},
            onDownvoteClicked = {},
            onShareClicked = {},
        )
    }
}
