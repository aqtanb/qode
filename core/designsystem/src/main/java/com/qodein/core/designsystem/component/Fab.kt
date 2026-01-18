package com.qodein.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.SizeTokens

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
    val fabContent: @Composable () -> Unit = {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier
                .size(SizeTokens.Fab.sizeSmall)
                .clip(CircleShape),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = ElevationTokens.large,
                pressedElevation = ElevationTokens.extraLarge,
                focusedElevation = ElevationTokens.large,
                hoveredElevation = ElevationTokens.extraLarge,
            ),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(SizeTokens.Fab.iconSize),
            )
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
