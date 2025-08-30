package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.shared.model.Tag

/**
 * Tag filter bottom sheet component
 * Allows users to select tags for filtering posts in a mobile-friendly bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagFilterBottomSheet(
    isVisible: Boolean,
    availableTags: List<Tag>,
    currentTags: Set<Tag>,
    onTagsSelected: (Set<Tag>) -> Unit,
    onAllTagsSelected: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    SharedFilterBottomSheet(
        isVisible = isVisible,
        title = stringResource(R.string.filter_tag_title),
        onDismiss = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // All Tags option
            FilterChip(
                selected = currentTags.isEmpty(),
                onClick = {
                    onAllTagsSelected()
                    onDismiss()
                },
                label = { Text(stringResource(R.string.filter_tag_all)) },
            )

            if (availableTags.isNotEmpty()) {
                HorizontalDivider()

                // Tags grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    for (tag in availableTags) {
                        val isSelected = currentTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newTags = if (isSelected) {
                                    currentTags - tag
                                } else {
                                    currentTags + tag
                                }
                                onTagsSelected(newTags)
                            },
                            label = {
                                Text(
                                    text = tag.name,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                )
                            },
                        )
                    }
                }
            }

            // Close button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_close))
            }

            // Bottom spacing for gesture area
            Spacer(modifier = Modifier.height(SpacingTokens.lg))
        }
    }
}
