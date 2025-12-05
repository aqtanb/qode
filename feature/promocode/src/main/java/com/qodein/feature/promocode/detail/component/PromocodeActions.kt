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
import com.qodein.feature.promocode.R
import com.qodein.shared.model.VoteState

@Composable
fun PromocodeActions(
    upvoteCount: Int,
    downvoteCount: Int,
    vote: VoteState,
    currentVoting: VoteState?,
    onVote: (VoteState) -> Unit,
    onShareClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val upvoteContentDescription = stringResource(R.string.cd_upvote, upvoteCount)
    val downvoteContentDescription = stringResource(R.string.cd_downvote, downvoteCount)
    val shareContentDescription = stringResource(R.string.cd_share)
    val isUpvoteVoting = currentVoting == VoteState.UPVOTE
    val isDownvoteVoting = currentVoting == VoteState.DOWNVOTE
    val votingDisabled = currentVoting != null

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QodeinFilterChip(
            label = upvoteCount.toString(),
            onClick = { if (!votingDisabled) onVote(VoteState.UPVOTE) },
            selected = vote == VoteState.UPVOTE,
            leadingIcon = {
                Icon(
                    imageVector = QodeActionIcons.Up,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            },
            isLoading = isUpvoteVoting,
            modifier = Modifier.semantics {
                contentDescription = upvoteContentDescription
            },
        )

        QodeinFilterChip(
            label = if (downvoteCount == 0) "0" else "-$downvoteCount",
            onClick = { if (!votingDisabled) onVote(VoteState.DOWNVOTE) },
            selected = vote == VoteState.DOWNVOTE,
            leadingIcon = {
                Icon(
                    imageVector = QodeActionIcons.Down,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            },
            isLoading = isDownvoteVoting,
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
        PromocodeActions(
            upvoteCount = 12,
            downvoteCount = 5,
            vote = VoteState.UPVOTE,
            onVote = {},
            onShareClicked = {},
            currentVoting = null,
        )
    }
}
