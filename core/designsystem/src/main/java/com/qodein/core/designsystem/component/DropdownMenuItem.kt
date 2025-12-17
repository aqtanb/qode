package com.qodein.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

@Composable
fun QodeDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    leadingIconContentDescription: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = SpacingTokens.md,
        vertical = SpacingTokens.sm,
    ),
    minHeight: Dp = SizeTokens.Button.heightXL
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        onClick = onClick,
        enabled = enabled,
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = leadingIconContentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight),
        contentPadding = contentPadding,
    )
}

@ThemePreviews
@Composable
private fun QodeDropdownMenuItemPreview() {
    QodeTheme {
        Surface {
            Column {
                QodeDropdownMenuItem(
                    text = "Block",
                    leadingIcon = UIIcons.Block,
                    onClick = {},
                )
                QodeDropdownMenuItem(
                    text = "Report",
                    leadingIcon = UIIcons.Report,
                    onClick = {},
                )
                QodeDropdownMenuItem(
                    text = "Disabled action",
                    leadingIcon = UIIcons.Block,
                    enabled = false,
                    onClick = {},
                )
            }
        }
    }
}
