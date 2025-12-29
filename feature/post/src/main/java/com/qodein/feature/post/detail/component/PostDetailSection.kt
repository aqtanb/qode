package com.qodein.feature.post.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import com.qodein.core.ui.preview.PostPreviewData
import com.qodein.feature.post.R
import com.qodein.feature.post.detail.PostDetailAction
import com.qodein.feature.post.feed.component.PostCard
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState

@Composable
internal fun PostDetailSection(
    post: Post,
    onAction: (PostDetailAction) -> Unit,
    userVoteState: VoteState,
    userId: UserId?,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        PostCard(
            post = post,
            onPostClick = {},
            onImageClick = onImageClick,
        )

        PostInteractionsRow(
            postId = post.id,
            upvotes = post.upvotes,
            downvotes = post.downvotes,
            userVoteState = userVoteState,
            userId = userId,
            onAction = onAction,
            modifier = Modifier.padding(horizontal = SpacingTokens.md),
        )
    }
}

@Composable
private fun PostInteractionsRow(
    postId: PostId,
    upvotes: Int,
    downvotes: Int,
    userVoteState: VoteState,
    userId: UserId?,
    onAction: (PostDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val upvoteContentDescription = stringResource(R.string.cd_upvote, upvotes)
    val downvoteContentDescription = stringResource(R.string.cd_downvote, downvotes)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QodeinFilterChip(
            label = upvotes.toString(),
            onClick = {
                onAction(
                    PostDetailAction.UpvoteClicked(
                        postId = postId.value,
                        currentVoteState = userVoteState,
                        userId = userId,
                    ),
                )
            },
            selected = userVoteState == VoteState.UPVOTE,
            leadingIcon = {
                Icon(
                    imageVector = QodeActionIcons.Up,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            },
            modifier = Modifier
                .semantics {
                    contentDescription = upvoteContentDescription
                },
        )

        QodeinFilterChip(
            label = downvotes.toString(),
            onClick = {
                onAction(
                    PostDetailAction.DownvoteClicked(
                        postId = postId.value,
                        currentVoteState = userVoteState,
                        userId = userId,
                    ),
                )
            },
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
    }
}

@ThemePreviews
@Composable
private fun PostDetailCardPreview() {
    QodeTheme {
        Surface {
            PostDetailSection(
                post = PostPreviewData.postWithLongEverything,
                onAction = {},
                userVoteState = VoteState.UPVOTE,
                userId = null,
                onImageClick = {},
            )
        }
    }
}
