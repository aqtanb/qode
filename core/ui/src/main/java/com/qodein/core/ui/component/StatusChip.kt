package com.qodein.core.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Reusable status chip component with icon and text.
 * Used for displaying user status, verification status, categories, etc.
 * Matches the exact design pattern from ServiceInfoSection.
 */
@Composable
fun StatusChip(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String? = null
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(50), // Fully circular pill
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription ?: text,
                tint = contentColor,
                modifier = Modifier.size(12.dp),
            )

            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "Status Chips - Light", showBackground = true)
@Preview(name = "Status Chips - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatusChipPreview() {
    val isSystemInDarkTheme = isSystemInDarkTheme()

    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Success/Verified Status
            StatusChip(
                text = "Verified",
                icon = Icons.Default.CheckCircle,
                backgroundColor = if (isSystemInDarkTheme) Color(0xFF1B4332) else Color(0xFFE8F5E8),
                contentColor = if (isSystemInDarkTheme) Color(0xFF4CAF50) else Color(0xFF2E7D32),
            )

            // Warning/Pending Status
            StatusChip(
                text = "Pending Review",
                icon = Icons.Default.Schedule,
                backgroundColor = if (isSystemInDarkTheme) Color(0xFF3D2914) else Color(0xFFFFF3E0),
                contentColor = if (isSystemInDarkTheme) Color(0xFFFF9800) else Color(0xFFE65100),
            )

            // Error/Alert Status
            StatusChip(
                text = "Action Required",
                icon = Icons.Default.Warning,
                backgroundColor = if (isSystemInDarkTheme) Color(0xFF3E1723) else Color(0xFFFFEBEE),
                contentColor = if (isSystemInDarkTheme) Color(0xFFF44336) else Color(0xFFC62828),
            )

            // Info/Category Status
            StatusChip(
                text = "Professional",
                icon = Icons.Default.Business,
                backgroundColor = if (isSystemInDarkTheme) Color(0xFF0D47A1).copy(alpha = 0.2f) else Color(0xFFE3F2FD),
                contentColor = if (isSystemInDarkTheme) Color(0xFF42A5F5) else Color(0xFF1565C0),
            )

            // Online/Active Status
            StatusChip(
                text = "Online",
                icon = Icons.Default.Circle,
                backgroundColor = if (isSystemInDarkTheme) Color(0xFF1B5E20).copy(alpha = 0.3f) else Color(0xFFE8F5E8),
                contentColor = if (isSystemInDarkTheme) Color(0xFF66BB6A) else Color(0xFF388E3C),
            )
        }
    }
}
