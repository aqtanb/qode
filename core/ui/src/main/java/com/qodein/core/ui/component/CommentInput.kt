package com.qodein.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Comment input component with user avatar and submit functionality
 */
@Composable
fun CommentInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false,
    canSubmit: Boolean = false,
    userAvatarUrl: String? = null,
    placeholder: String = "Add a comment...",
    maxLines: Int = 4
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Only expand when actually needed - focused OR has content
    val isExpanded = isFocused || text.isNotEmpty()

    val elevation by animateFloatAsState(
        targetValue = if (isFocused) 4f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "elevation",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = elevation.dp,
        border = if (isFocused) {
            BorderStroke(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            )
        } else {
            BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                // User avatar
                AsyncImage(
                    model = userAvatarUrl ?: "https://picsum.photos/seed/currentuser/150/150",
                    contentDescription = "Your avatar",
                    modifier = Modifier
                        .size(SizeTokens.Avatar.sizeSmall)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = CircleShape,
                        ),
                )

                Spacer(modifier = Modifier.width(SpacingTokens.sm))

                // Text input
                Column(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        maxLines = if (isExpanded) maxLines else 1,
                        interactionSource = interactionSource,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = if (canSubmit) ImeAction.Send else ImeAction.Default,
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = if (canSubmit) {
                                {
                                    onSubmit()
                                    keyboardController?.hide()
                                }
                            } else {
                                null
                            },
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(ShapeTokens.Corner.medium),
                                    )
                                    .padding(SpacingTokens.md),
                                contentAlignment = if (isExpanded) Alignment.TopStart else Alignment.CenterStart,
                            ) {
                                if (text.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }
            }

            // Action row - only show when focused or has text
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SpacingTokens.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Character count
                    Text(
                        text = "${text.length}/1000",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (text.length > 900) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Clear button - only show when there's text
                        if (text.isNotEmpty()) {
                            Surface(
                                onClick = {
                                    onClear()
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        imageVector = QodeActionIcons.Clear,
                                        contentDescription = "Clear comment",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }

                        // Submit button
                        Surface(
                            onClick = {
                                if (canSubmit && !isSubmitting) {
                                    onSubmit()
                                    keyboardController?.hide()
                                }
                            },
                            shape = CircleShape,
                            color = if (canSubmit) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(40.dp),
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                } else {
                                    Icon(
                                        imageVector = QodeActionIcons.Send,
                                        contentDescription = "Post comment",
                                        tint = if (canSubmit) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        },
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentInputPreview() {
    QodeTheme {
        CommentInput(
            text = "This looks like an amazing deal! Thanks for sharing.",
            onTextChange = {},
            onSubmit = {},
            onClear = {},
            canSubmit = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentInputEmptyPreview() {
    QodeTheme {
        CommentInput(
            text = "",
            onTextChange = {},
            onSubmit = {},
            onClear = {},
            canSubmit = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentInputFocusedEmptyPreview() {
    QodeTheme {
        // This simulates the focused state for preview
        CommentInput(
            text = "",
            onTextChange = {},
            onSubmit = {},
            onClear = {},
            canSubmit = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}
