package com.qodein.feature.post.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

@Composable
fun FeedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search posts...",
    isActive: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val containerColor by animateColorAsState(
        targetValue = when {
            isActive || isFocused -> MaterialTheme.colorScheme.surface
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(),
        label = "container_color",
    )

    val borderRadius by animateDpAsState(
        targetValue = if (isActive || isFocused) ShapeTokens.Corner.large else ShapeTokens.Corner.full,
        animationSpec = spring(),
        label = "border_radius",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(borderRadius),
        color = containerColor,
        shadowElevation = if (isActive || isFocused) 2.dp else 0.dp,
        tonalElevation = if (isActive || isFocused) 2.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            // Search Icon
            Icon(
                imageVector = QodeNavigationIcons.Search,
                contentDescription = "Search",
                modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                tint = if (isActive || isFocused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )

            // Search Input Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "Search posts input field"
                    },
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearchClick()
                            keyboardController?.hide()
                        },
                    ),
                )

                // Placeholder
                if (query.isEmpty() && !isFocused) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Clear Button (when there's text)
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(SizeTokens.IconButton.sizeMedium),
                ) {
                    Icon(
                        imageVector = QodeActionIcons.ClearCircled,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Filter Button
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.size(SizeTokens.IconButton.sizeMedium),
            ) {
                Icon(
                    imageVector = QodeNavigationIcons.Filter,
                    contentDescription = "Filter posts",
                    modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun CompactSearchBar(
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(ShapeTokens.Corner.full))
            .clickable { onSearchClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            // Search Icon
            Icon(
                imageVector = QodeNavigationIcons.Search,
                contentDescription = "Search",
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Placeholder Text
            Text(
                text = "Search posts...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )

            // Filter Button
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.size(SizeTokens.IconButton.sizeSmall),
            ) {
                Icon(
                    imageVector = QodeNavigationIcons.Filter,
                    contentDescription = "Filter posts",
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedSearchBarPreview() {
    QodeTheme {
        var query by remember { mutableStateOf("") }
        var isExpanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .padding(SpacingTokens.md)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            Text(
                "Search Bar States:",
                style = MaterialTheme.typography.titleMedium,
            )

            // Default state
            FeedSearchBar(
                query = "",
                onQueryChange = {},
                onSearchClick = {},
                onFilterClick = {},
                isActive = false,
            )

            // Active state with text
            FeedSearchBar(
                query = "android development",
                onQueryChange = {},
                onSearchClick = {},
                onFilterClick = {},
                isActive = true,
            )

            // Focused state
            FeedSearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearchClick = {},
                onFilterClick = {},
                isActive = isExpanded,
            )

            Text(
                "Compact Search Bar:",
                style = MaterialTheme.typography.titleMedium,
            )

            // Compact version
            CompactSearchBar(
                onSearchClick = { isExpanded = true },
                onFilterClick = {},
            )
        }
    }
}
