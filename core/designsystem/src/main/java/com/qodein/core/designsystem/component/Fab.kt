package com.qodein.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Qodein-branded Floating Action Button with consistent styling across the app.
 *
 * Features:
 * - Consistent Material 3 styling with brand colors
 * - Optional auto-hiding behavior based on scroll state
 * - Supports custom icons and click handlers
 *
 * @param onClick Callback invoked when the FAB is clicked
 * @param icon Icon to display inside the FAB
 * @param contentDescription Accessibility description for the icon
 * @param modifier Optional modifier for additional styling
 * @param autoHide Whether to enable auto-hiding behavior on scroll
 * @param autoHideState State controlling auto-hiding behavior (required when autoHide is true)
 * @param autoHideDirection Direction of auto-hide animation (UP or DOWN)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QodeinFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    autoHide: Boolean = false,
    autoHideState: AutoHidingState? = null,
    autoHideDirection: AutoHideDirection = AutoHideDirection.UP
) {
    Box(modifier = modifier) {
        val fabContent: @Composable () -> Unit = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                tooltip = { PlainTooltip { Text(contentDescription) } },
                state = rememberTooltipState(),
            ) {
                FloatingActionButton(
                    onClick = onClick,
                    modifier = Modifier.clip(CircleShape),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                    )
                }
            }
        }

        if (autoHide && autoHideState != null) {
            AutoHidingContent(
                state = autoHideState,
                direction = autoHideDirection,
            ) {
                fabContent()
            }
        } else {
            fabContent()
        }
    }
}
