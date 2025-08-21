package com.qodein.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.shared.model.Service
import kotlinx.coroutines.delay

data class TypeableDropdownItem(
    val id: String,
    val text: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val isPopular: Boolean = false
)

@Composable
fun TypeableDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    onItemSelected: (TypeableDropdownItem) -> Unit,
    items: List<TypeableDropdownItem>,
    modifier: Modifier = Modifier,
    label: String = "Select an option",
    placeholder: String = "Type to search...",
    isLoading: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    maxDropdownHeight: Int = 200
) {
    var expanded by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Auto-expand when focused and has items
    LaunchedEffect(hasFocus, items.isNotEmpty()) {
        if (hasFocus && items.isNotEmpty() && value.isNotEmpty()) {
            expanded = true
        }
    }

    // Auto-collapse when focus is lost
    LaunchedEffect(hasFocus) {
        if (!hasFocus) {
            delay(150) // Small delay to allow item selection
            expanded = false
        }
    }

    Column(modifier = modifier) {
        // Text field with dropdown indicator
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                expanded = newValue.isNotEmpty() && items.isNotEmpty()
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            enabled = enabled,
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    hasFocus = focusState.isFocused
                },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = if (hasFocus) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Loading indicator
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(SpacingTokens.sm))
                    }

                    // Clear button
                    if (value.isNotEmpty() && enabled) {
                        IconButton(
                            onClick = {
                                onValueChange("")
                                expanded = false
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Dropdown indicator
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .rotate(if (expanded) 180f else 0f)
                            .clickable {
                                expanded = !expanded
                                if (!expanded) focusManager.clearFocus()
                            },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
            ),
            singleLine = true,
        )

        // Error message
        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(SpacingTokens.xs))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = SpacingTokens.md),
            )
        }

        // Dropdown menu
        AnimatedVisibility(
            visible = expanded && items.isNotEmpty(),
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ) + fadeIn(animationSpec = tween(200)),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ) + fadeOut(animationSpec = tween(150)),
            modifier = Modifier.zIndex(1f),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxDropdownHeight.dp)
                    .padding(top = SpacingTokens.xs),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp,
                ),
                shape = RoundedCornerShape(SpacingTokens.md),
            ) {
                LazyColumn(
                    modifier = Modifier.padding(SpacingTokens.xs),
                ) {
                    items(items, key = { it.id }) { item ->
                        DropdownItem(
                            item = item,
                            isSelected = item.text == value,
                            onClick = {
                                onItemSelected(item)
                                onValueChange(item.text)
                                expanded = false
                                focusManager.clearFocus()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownItem(
    item: TypeableDropdownItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SpacingTokens.sm))
            .clickable { onClick() }
            .animateContentSize(),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(SpacingTokens.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                // Icon
                if (item.icon != null) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Spacer(modifier = Modifier.width(SpacingTokens.md))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        // Popular badge
                        if (item.isPopular) {
                            Spacer(modifier = Modifier.width(SpacingTokens.xs))
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = RoundedCornerShape(SpacingTokens.xs),
                                modifier = Modifier.padding(horizontal = SpacingTokens.xs),
                            ) {
                                Text(
                                    text = "Popular",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.padding(
                                        horizontal = SpacingTokens.xs,
                                        vertical = 2.dp,
                                    ),
                                )
                            }
                        }
                    }

                    // Subtitle (category)
                    if (item.subtitle != null) {
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// Extension function to convert Service to TypeableDropdownItem
fun Service.toDropdownItem(): TypeableDropdownItem =
    TypeableDropdownItem(
        id = id.value,
        text = name,
        subtitle = category,
        icon = QodeCommerceIcons.PromoCode, // Default icon, can be customized per category
        isPopular = isPopular,
    )

@Preview(showBackground = true)
@Composable
private fun TypeableDropdownPreview() {
    QodeTheme {
        val sampleItems = listOf(
            TypeableDropdownItem("1", "Netflix", "Streaming", QodeCommerceIcons.PromoCode, true),
            TypeableDropdownItem("2", "Spotify", "Music", QodeCommerceIcons.PromoCode, true),
            TypeableDropdownItem("3", "Kaspi", "Finance", QodeCommerceIcons.PromoCode, false),
            TypeableDropdownItem("4", "Glovo", "Food & Delivery", QodeCommerceIcons.PromoCode, false),
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TypeableDropdown(
                value = "Netflix",
                onValueChange = {},
                onItemSelected = {},
                items = sampleItems,
                label = "Service",
                placeholder = "Search for a service...",
            )
        }
    }
}
