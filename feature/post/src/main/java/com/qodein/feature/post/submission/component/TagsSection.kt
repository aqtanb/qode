package com.qodein.feature.post.submission.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeinChip
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.PostIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Tags input component with popular tags and custom tag entry
 *
 * @param selectedTags List of currently selected tags
 * @param customTagInput Current value of the custom tag input field
 * @param onCustomTagInputChange Called when custom tag input changes
 * @param onTagSelected Called when a tag is selected
 * @param onTagRemoved Called when a tag is removed
 * @param modifier Modifier to be applied to the component
 * @param popularTags List of popular tags to display
 * @param maxTags Maximum number of tags allowed (default 10)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsSection(
    selectedTags: List<String>,
    customTagInput: String,
    onCustomTagInputChange: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    modifier: Modifier = Modifier,
    popularTags: List<String> = emptyList(),
    maxTags: Int = 10,
    onDone: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Header: Icon box + "Add Tags" text
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(SizeTokens.IconButton.sizeMedium)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = PostIcons.Hashtag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
            }

            Text(
                text = "Add Tags",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Popular tags section
        if (popularTags.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                Text(
                    text = "Popular Tags",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    maxItemsInEachRow = 4,
                ) {
                    popularTags.take(10).forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        val canAdd = selectedTags.size < maxTags

                        QodeinChip(
                            label = tag,
                            onClick = {
                                if (isSelected) {
                                    onTagRemoved(tag)
                                } else if (canAdd) {
                                    onTagSelected(tag)
                                }
                            },
                            selected = isSelected,
                            enabled = isSelected || canAdd,
                        )
                    }
                }
            }
        }

        // Custom tag input
        QodeinTextField(
            value = customTagInput,
            onValueChange = onCustomTagInputChange,
            placeholder = "Type the tag",
            leadingIcon = PostIcons.Hashtag,
            keyboardActions = KeyboardActions(
                onDone = {
                    if (customTagInput.isNotBlank()) {
                        onTagSelected(customTagInput)
                    } else {
                        onDone()
                    }
                },
            ),
        )

        // Selected tags section
        if (selectedTags.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    selectedTags.forEach { tag ->
                        QodeinChip(
                            label = tag,
                            onClick = { },
                            selected = true,
                            onClose = { onTagRemoved(tag) },
                        )
                    }
                }
            }
        }
    }
}

// Preview
@Preview(name = "Tags Input", showBackground = true)
@Composable
private fun TagsInputPreview() {
    QodeTheme {
        TagsSection(
            selectedTags = listOf("#tech", "#food"),
            customTagInput = "",
            onCustomTagInputChange = {},
            onTagSelected = {},
            onTagRemoved = {},
            modifier = Modifier.padding(SpacingTokens.md),
            popularTags = listOf(
                "#trending",
                "#lifestyle",
                "#tech",
                "#nature",
                "#food",
                "#travel",
                "#fashion",
                "#fitness",
                "#gaming",
                "#shopping",
            ),
            onDone = { },
        )
    }
}
