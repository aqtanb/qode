package com.qodein.feature.post.submission.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinFilterChip
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.PostIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.post.R
import com.qodein.shared.common.Result
import com.qodein.shared.model.Tag

// TODO: Fix scrolling and imepadding
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TagSelectorBottomSheet(
    selectedTags: List<Tag>,
    onTagSelected: (Tag) -> Unit,
    onTagRemoved: (Tag) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    popularTags: List<String> = emptyList()
) {
    var customTagInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        TagSelectorContent(
            selectedTags = selectedTags,
            customTagInput = customTagInput,
            onCustomTagInputChange = { customTagInput = it },
            onAddCustomTag = {
                val tagResult = Tag.create(customTagInput)
                if (tagResult is Result.Success) {
                    onTagSelected(tagResult.data)
                    customTagInput = ""
                }
            },
            onTagSelected = onTagSelected,
            onTagRemoved = onTagRemoved,
            popularTags = popularTags,
            modifier = modifier,
        )
    }
}

@Composable
private fun TagSelectorContent(
    selectedTags: List<Tag>,
    customTagInput: String,
    onCustomTagInputChange: (String) -> Unit,
    onAddCustomTag: () -> Unit,
    onTagSelected: (Tag) -> Unit,
    onTagRemoved: (Tag) -> Unit,
    popularTags: List<String>,
    modifier: Modifier = Modifier,
    onClearAll: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    val isMaxTagsReached = selectedTags.size >= Tag.MAX_TAGS_SELECTED
    val isExistingTag = selectedTags.any { it.value == customTagInput }
    val containsInvalidCharacters = customTagInput.isNotEmpty() && !customTagInput.matches(Tag.VALID_PATTERN)
    val isTagTooLong = customTagInput.length > Tag.MAX_LENGTH

    // Error and helper text logic
    val errorText = when {
        isTagTooLong -> stringResource(R.string.tag_too_long)
        containsInvalidCharacters -> stringResource(R.string.tag_invalid_characters)
        isExistingTag && customTagInput.isNotEmpty() -> stringResource(R.string.tag_already_added)
        else -> null
    }

    val helperText = when {
        errorText != null -> null // Error takes priority
        isMaxTagsReached -> stringResource(R.string.max_tags_reached, Tag.MAX_TAGS_SELECTED)
        else -> stringResource(R.string.tag_helper_text)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.lg)
            .padding(bottom = SpacingTokens.xl),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        QodeinTextField(
            value = customTagInput.lowercase(),
            onValueChange = onCustomTagInputChange,
            placeholder = stringResource(R.string.type_tag),
            leadingIcon = PostIcons.Hashtag,
            helperText = helperText,
            errorText = errorText,
            enabled = !isMaxTagsReached,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.None,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (!isMaxTagsReached && errorText == null && customTagInput.isNotEmpty()) {
                        onAddCustomTag()
                    }
                },
            ),
        )

        val validPopularTags = remember(popularTags) {
            popularTags.mapNotNull { tagValue ->
                when (val result = Tag.create(tagValue)) {
                    is Result.Success -> result.data to tagValue
                    is Result.Error -> null
                }
            }
        }

        AnimatedVisibility(
            visible = validPopularTags.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = SpacingTokens.sm),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    Text(
                        text = stringResource(R.string.popular_tags),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    // TODO: Implement moving chip to the selected section when selected
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    ) {
                        validPopularTags.forEach { (tag, displayValue) ->
                            val isSelected = selectedTags.contains(tag)
                            val canAdd = selectedTags.size < Tag.MAX_TAGS_SELECTED
                            // TODO: Improve QodeinChip
                            QodeinFilterChip(
                                label = "#$displayValue",
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (isSelected) {
                                        onTagRemoved(tag)
                                    } else if (canAdd) {
                                        onTagSelected(tag)
                                    }
                                },
                                selected = isSelected,
                                filled = isSelected,
                                enabled = isSelected || canAdd,
                            )
                        }
                    }
                }
            }
        }

        if (selectedTags.isNotEmpty()) {
            Column {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = SpacingTokens.sm),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                )
                // TODO: Implement onClearAll
                Column(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    Text(
                        text = stringResource(R.string.selected) + " (${selectedTags.size}/${Tag.MAX_TAGS_SELECTED})",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    // TODO: Implementing editing on long press
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    ) {
                        selectedTags.forEach { tag ->
                            QodeinFilterChip(
                                label = "#${tag.value}",
                                onClick = { },
                                selected = true,
                                filled = true,
                                onClose = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onTagRemoved(tag)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TagSelectorContentPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(SpacingTokens.lg),
        ) {
            TagSelectorContent(
                selectedTags = listOf(
                    Tag("tech"),
                    Tag("lifestyle"),
                ),
                customTagInput = "",
                onCustomTagInputChange = {},
                onAddCustomTag = {},
                onTagSelected = {},
                onTagRemoved = {},
                popularTags = listOf(
                    "tech",
                    "food",
                    "travel",
                    "lifestyle",
                    "fashion",
                    "gaming",
                    "music",
                    "sports",
                ),
            )
        }
    }
}
