package com.qodein.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.icon.NavigationIcons
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens

data class QodeinMenuItem(
    val text: String,
    val onClick: () -> Unit,
    val leadingIcon: ImageVector,
    val trailingIcon: ImageVector? = null,
    val enabled: Boolean = true
)

data class QodeinMenuGroup(val items: List<QodeinMenuItem>, val containerColor: Color? = null)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QodeinDropdownMenuGrouped(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    groups: List<QodeinMenuGroup>,
    modifier: Modifier = Modifier
) {
    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.widthIn(min = SizeTokens.Menu.minWidth),
    ) {
        val groupCount = groups.size

        groups.forEachIndexed { groupIndex, group ->
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(groupIndex, groupCount),
                containerColor = group.containerColor ?: MenuDefaults.groupStandardContainerColor,
            ) {
                val groupItemCount = group.items.size
                group.items.forEachIndexed { itemIndex, menuItem ->
                    val itemShapes = MenuDefaults.itemShape(itemIndex, groupItemCount)

                    DropdownMenuItem(
                        onClick = {
                            menuItem.onClick()
                            onDismissRequest()
                        },
                        text = {
                            Text(
                                text = menuItem.text,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        shape = itemShapes.shape,
                        leadingIcon = {
                            Icon(
                                imageVector = menuItem.leadingIcon,
                                contentDescription = null,
                            )
                        },
                        trailingIcon = menuItem.trailingIcon?.let { icon ->
                            {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                )
                            }
                        },
                        enabled = menuItem.enabled,
                    )
                }
            }

            if (groupIndex != groupCount - 1) {
                Spacer(Modifier.height(MenuDefaults.GroupSpacing))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@PreviewLightDark
@Composable
private fun QodeinDropdownMenuGroupedPreview() {
    QodeTheme {
        Surface {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                QodeinDropdownMenuGrouped(
                    expanded = true,
                    onDismissRequest = {},
                    groups = listOf(
                        QodeinMenuGroup(
                            items = listOf(
                                QodeinMenuItem(
                                    text = "Blocked Users",
                                    onClick = {},
                                    leadingIcon = UIIcons.Block,
                                    trailingIcon = NavigationIcons.ChevronRight,
                                ),
                            ),
                        ),
                        QodeinMenuGroup(
                            items = listOf(
                                QodeinMenuItem(
                                    text = "Sign Out",
                                    onClick = {},
                                    leadingIcon = ActionIcons.SignOut,
                                    trailingIcon = NavigationIcons.ChevronRight,
                                ),
                            ),
                            containerColor = MenuDefaults.groupVibrantContainerColor,
                        ),
                    ),
                )
            }
        }
    }
}
