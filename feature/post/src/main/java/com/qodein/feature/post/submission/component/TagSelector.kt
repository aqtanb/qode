package com.qodein.feature.post.submission.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeinIcons
import com.qodein.core.designsystem.theme.OpacityTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.post.R
import com.qodein.shared.model.Tag

@Composable
internal fun TagSelector(
    selectedTags: List<Tag>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ShapeTokens.Corner.large))
            .background(MaterialTheme.colorScheme.background.copy(alpha = OpacityTokens.HOVER))
            .border(
                width = ShapeTokens.Border.thin,
                color = MaterialTheme.colorScheme.outline.copy(alpha = OpacityTokens.DIVIDER),
                shape = RoundedCornerShape(ShapeTokens.Corner.large),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        Box(
            modifier = Modifier
                .size(SizeTokens.IconButton.sizeSmall)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = QodeinIcons.Hashtag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
            )
        }

        Text(
            text = if (selectedTags.isEmpty()) {
                stringResource(R.string.select_tags)
            } else {
                selectedTags.joinToString(", ") { it.value }
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selectedTags.isEmpty()) FontWeight.Normal else FontWeight.Medium,
            ),
            color = if (selectedTags.isEmpty()) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Icon(
            imageVector = QodeActionIcons.Next,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
        )
    }
}

@PreviewLightDark
@Composable
private fun TagSelectorPreview() {
    QodeTheme {
        Surface {
            Column(
                modifier = Modifier.padding(SpacingTokens.md),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                TagSelector(
                    selectedTags = emptyList(),
                    onClick = {},
                )

                TagSelector(
                    selectedTags = listOf(Tag("tech")),
                    onClick = {},
                )

                TagSelector(
                    selectedTags = listOf(
                        Tag("tech"),
                        Tag("lifestyle"),
                        Tag("coding"),
                    ),
                    onClick = {},
                )
            }
        }
    }
}
