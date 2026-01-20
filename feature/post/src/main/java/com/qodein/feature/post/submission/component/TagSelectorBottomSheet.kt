package com.qodein.feature.post.submission.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.qodein.core.designsystem.component.QodeinFilterChip
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.PostIcons
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.post.R
import com.qodein.shared.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TagSelectorBottomSheet(
    selectedTags: List<String>,
    currentTagInput: String,
    onTagChange: (String) -> Unit,
    onTagAdded: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        TagSelectorContent(
            selectedTags = selectedTags,
            currentTagInput = currentTagInput,
            onTagChange = onTagChange,
            onTagAdded = onTagAdded,
            onTagRemoved = onTagRemoved,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}

@Composable
private fun TagSelectorContent(
    selectedTags: List<String>,
    currentTagInput: String,
    onTagChange: (String) -> Unit,
    onTagAdded: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.lg)
            .padding(bottom = SpacingTokens.xl)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        QodeinTextField(
            value = currentTagInput,
            onValueChange = { onTagChange(it) },
            placeholder = stringResource(R.string.post_tag_placeholder),
            leadingIcon = PostIcons.Hashtag,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.None,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (currentTagInput.isBlank()) onDismiss()
                    onTagAdded(currentTagInput)
                },
            ),
            canBeBlank = true,
        )

        if (selectedTags.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                Text(
                    text = stringResource(R.string.selected_tags_label) + " (${selectedTags.size}/${Post.MAX_TAGS})",
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
                        QodeinFilterChip(
                            label = "#$tag",
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
