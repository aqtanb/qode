package com.qodein.feature.post.submission.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinChip
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.PostIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.post.R
import com.qodein.shared.common.Result
import com.qodein.shared.model.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TagSelectorBottomSheet(
    selectedTags: List<Tag>,
    onTagSelected: (Tag) -> Unit,
    onTagRemoved: (Tag) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    popularTags: List<String> = emptyList(),
    maxTags: Int = 10
) {
    var customTagInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.lg)
                .padding(bottom = SpacingTokens.xl),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            Text(
                text = stringResource(R.string.select_tags),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            QodeinTextField(
                value = customTagInput,
                onValueChange = { customTagInput = it },
                placeholder = stringResource(R.string.type_tag),
                leadingIcon = PostIcons.Hashtag,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (customTagInput.isNotBlank() && selectedTags.size < maxTags) {
                            val trimmedTag = customTagInput.trim().removePrefix("#")
                            val tagResult = Tag.create(trimmedTag)
                            if (tagResult is Result.Success) {
                                onTagSelected(tagResult.data)
                                customTagInput = ""
                            }
                        }
                    },
                ),
            )

            val validPopularTags = remember(popularTags) {
                popularTags.mapNotNull { tagValue ->
                    val cleanValue = tagValue.removePrefix("#")
                    when (val result = Tag.create(cleanValue)) {
                        is Result.Success -> result.data to tagValue
                        is Result.Error -> null
                    }
                }
            }

            if (validPopularTags.isNotEmpty()) {
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

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    ) {
                        validPopularTags.forEach { (tag, displayValue) ->
                            val isSelected = selectedTags.contains(tag)
                            val canAdd = selectedTags.size < maxTags

                            QodeinChip(
                                label = displayValue,
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

            if (selectedTags.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    Text(
                        text = stringResource(R.string.selected),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    ) {
                        selectedTags.forEach { tag ->
                            QodeinChip(
                                label = tag.value,
                                onClick = { },
                                selected = true,
                                onClose = { onTagRemoved(tag) },
                            )
                        }
                    }

                    if (selectedTags.size >= maxTags) {
                        Text(
                            text = stringResource(R.string.max_tags_reached, maxTags),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun TagSelectorBottomSheetPreview() {
    QodeTheme {
        TagSelectorBottomSheet(
            selectedTags = listOf(
                Tag("tech"),
                Tag("lifestyle"),
            ),
            onTagSelected = {},
            onTagRemoved = {},
            onDismiss = {},
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
