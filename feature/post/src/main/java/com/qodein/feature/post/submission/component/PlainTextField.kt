package com.qodein.feature.post.submission.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.qodein.core.designsystem.theme.SpacingTokens

@Composable
internal fun PlainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val finalTextStyle = textStyle.copy(
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = textStyle.fontSize * 1.4,
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = finalTextStyle,
        modifier = modifier.padding(vertical = SpacingTokens.xs),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = finalTextStyle.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    ),
                )
            }
            innerTextField()
        },
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    )
}
