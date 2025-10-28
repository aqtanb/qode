package com.qodein.feature.post.detail.component

import android.R.attr.label
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PostPreviewData
import com.qodein.feature.post.R
import com.qodein.feature.post.detail.PostDetailAction
import com.qodein.feature.post.feed.component.PostCard
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId

@Composable
internal fun PostDetailSection(
    post: Post,
    onAction: (PostDetailAction) -> Unit,
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
            commentCount = post.commentCount,
            isUpvoted = false,
            isDownvoted = false,
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
    commentCount: Int,
    isUpvoted: Boolean,
    isDownvoted: Boolean,
    onAction: (PostDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val upvoteContentDescription = stringResource(R.string.cd_upvote, upvotes)
    val downvoteContentDescription = stringResource(R.string.cd_downvote, downvotes)
    val commentsContentDescription = stringResource(R.string.cd_comments, commentCount)
    val shareContentDescription = stringResource(R.string.cd_share)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QodeinFilterChip(
            label = upvotes.toString(),
            onClick = { },
            selected = isUpvoted,
            leadingIcon = QodeActionIcons.Up,
            modifier = Modifier
                .semantics {
                    contentDescription = upvoteContentDescription
                },
        )

        QodeinFilterChip(
            label = downvotes.toString(),
            onClick = {},
            selected = isDownvoted,
            leadingIcon = QodeActionIcons.Down,
            modifier = Modifier.semantics {
                contentDescription = downvoteContentDescription
            },
        )

        QodeinFilterChip(
            label = commentCount.toString(),
            onClick = {},
            selected = false,
            leadingIcon = QodeActionIcons.Comment,
            modifier = Modifier.semantics {
                contentDescription = commentsContentDescription
            },
        )

        Spacer(modifier = Modifier.weight(1f))

        QodeinFilterChip(
            label = "Share",
            onClick = {},
            selected = false,
            leadingIcon = QodeActionIcons.Share,
            modifier = Modifier.semantics {
                contentDescription = shareContentDescription
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
                onImageClick = {},
            )
        }
    }
}
