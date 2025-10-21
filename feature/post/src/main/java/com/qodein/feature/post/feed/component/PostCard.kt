package com.qodein.feature.post.feed.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinAssistChip
import com.qodein.core.designsystem.component.QodeinElevatedCard
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PostPreviewData
import com.qodein.feature.post.component.PostImage
import com.qodein.shared.model.Post

@Composable
fun PostCard(
    post: Post,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    QodeinElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        onClick = { onPostClick(post.id.value) },
        contentPadding = PaddingValues(SpacingTokens.lg),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            if (post.tags.isNotEmpty()) {
                PostTagsRow(tags = post.tags.map { it.value })
            }

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            if (post.imageUrls.isNotEmpty()) {
                HorizontalPager(
                    state = rememberPagerState { post.imageUrls.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SpacingTokens.xs),
                ) { page ->
                    PostImage(
                        uri = post.imageUrls[page],
                        currentPage = page + 1,
                        totalPages = post.imageUrls.size,
                        onClick = { },
                    )
                }
            } else if (!post.content.isNullOrBlank()) {
                Text(
                    text = post.content!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PostTagsRow(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        tags.forEach { tag ->
            QodeinAssistChip(
                label = "#$tag",
                onClick = { },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PostCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            PostCard(
                post = PostPreviewData.postWithImages,
                onPostClick = {},
            )

            PostCard(
                post = PostPreviewData.postWithoutImages,
                onPostClick = {},
            )
        }
    }
}
