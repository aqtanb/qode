package com.qodein.core.designsystem.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeCardWithActions
import com.qodein.core.designsystem.component.QodeChip
import com.qodein.core.designsystem.component.QodeChipVariant
import com.qodein.core.designsystem.component.QodeExpandableCard
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.component.QodeListCard
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.component.QodeTextFieldState
import com.qodein.core.designsystem.component.QodeTextFieldVariant
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme

/**
 * Comprehensive preview of all Qode design system components
 */
@OptIn(ExperimentalLayoutApi::class)
@Preview(
    name = "Qode Design System - Light Theme",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 360,
    heightDp = 1200,
)
@Composable
fun QodeDesignSystemLightPreview() {
    QodeTheme(darkTheme = false) {
        DesignSystemShowcase()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(
    name = "Qode Design System - Dark Theme",
    showBackground = true,
    backgroundColor = 0xFF121212,
    widthDp = 360,
    heightDp = 1200,
)
@Composable
fun QodeDesignSystemDarkPreview() {
    QodeTheme(darkTheme = true) {
        DesignSystemShowcase()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DesignSystemShowcase() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(QodeSpacing.md),
        ) {
            // Header
            Text(
                text = "Qode Design System",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = "Production-ready components for the Qode app",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = QodeSpacing.lg),
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = QodeSpacing.md),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            // Buttons Section
            SectionTitle("Buttons")

            ComponentGroup("Button Variants") {
                QodeButton(
                    onClick = {},
                    text = "Primary Button",
                )
                QodeButton(
                    onClick = {},
                    text = "Secondary",
                    variant = QodeButtonVariant.Secondary,
                )
                QodeButton(
                    onClick = {},
                    text = "Text Button",
                    variant = QodeButtonVariant.Text,
                )
                QodeButton(
                    onClick = {},
                    text = "Outlined",
                    variant = QodeButtonVariant.Outlined,
                )
            }

            ComponentGroup("Button States") {
                QodeButton(
                    onClick = {},
                    text = "Loading",
                    loading = true,
                )
                QodeButton(
                    onClick = {},
                    text = "Disabled",
                    enabled = false,
                )
                QodeButton(
                    onClick = {},
                    text = "With Icon",
                    leadingIcon = Icons.Default.Add,
                )
            }

            ComponentGroup("Icon Buttons") {
                QodeIconButton(
                    onClick = {},
                    icon = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    variant = QodeButtonVariant.Primary,
                )
                QodeIconButton(
                    onClick = {},
                    icon = Icons.Default.Share,
                    contentDescription = "Share",
                    variant = QodeButtonVariant.Secondary,
                )
                QodeIconButton(
                    onClick = {},
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete",
                    variant = QodeButtonVariant.Text,
                )
                QodeIconButton(
                    onClick = {},
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit",
                    variant = QodeButtonVariant.Outlined,
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = QodeSpacing.md),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            // Text Fields Section
            SectionTitle("Text Fields")

            var textValue by remember { mutableStateOf("") }
            var passwordValue by remember { mutableStateOf("") }
            var searchValue by remember { mutableStateOf("") }

            ComponentColumn {
                QodeTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = "Store Name",
                    placeholder = "Enter store name...",
                    helperText = "The name of the store offering the promo code",
                )

                QodeTextField(
                    value = "",
                    onValueChange = {},
                    label = "Required Field",
                    required = true,
                    state = QodeTextFieldState.Error("This field is required"),
                )

                QodeTextField(
                    value = "SAVE20",
                    onValueChange = {},
                    label = "Promo Code",
                    state = QodeTextFieldState.Success,
                    helperText = "Valid promo code!",
                )

                QodeTextField(
                    value = searchValue,
                    onValueChange = { searchValue = it },
                    placeholder = "Search promo codes...",
                    variant = QodeTextFieldVariant.Search,
                )

                QodeTextField(
                    value = passwordValue,
                    onValueChange = { passwordValue = it },
                    label = "Password",
                    variant = QodeTextFieldVariant.Password,
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = QodeSpacing.md),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            // Chips Section
            SectionTitle("Chips")

            ComponentGroup("Filter Chips") {
                QodeChip(
                    label = "All",
                    variant = QodeChipVariant.Filter,
                    selected = true,
                    onClick = {},
                )
                QodeChip(
                    label = "Electronics",
                    variant = QodeChipVariant.Filter,
                    onClick = {},
                )
                QodeChip(
                    label = "Fashion",
                    variant = QodeChipVariant.Filter,
                    onClick = {},
                )
            }

            ComponentGroup("Suggestion Chips") {
                QodeChip(
                    label = "Popular",
                    variant = QodeChipVariant.Suggestion,
                    leadingIcon = Icons.Default.Star,
                    onClick = {},
                )
                QodeChip(
                    label = "New",
                    variant = QodeChipVariant.Suggestion,
                    onClick = {},
                )
            }

            ComponentGroup("Input Chips") {
                QodeChip(
                    label = "Kaspi Bank",
                    onClick = {}, // ADD THIS
                    variant = QodeChipVariant.Input,
                    onClose = {},
                )
                QodeChip(
                    label = "Magnum",
                    onClick = {}, // ADD THIS
                    variant = QodeChipVariant.Input,
                    leadingIcon = Icons.Default.ShoppingCart,
                    onClose = {},
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = QodeSpacing.md),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            // Cards Section
            SectionTitle("Cards")

            ComponentColumn {
                QodeCard(variant = QodeCardVariant.Elevated) {
                    Text(
                        "20% OFF",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "Electronics & Gadgets",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        "Valid until Dec 31, 2024",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                var expanded by remember { mutableStateOf(false) }
                QodeExpandableCard(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    title = {
                        Text("Terms & Conditions", style = MaterialTheme.typography.titleMedium)
                    },
                    expandedContent = {
                        Text(
                            "• Valid for new customers only\n" +
                                "• Minimum purchase: 10,000 KZT\n" +
                                "• Cannot be combined with other offers",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                )

                QodeCardWithActions(
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "FREESHIP",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    "Free shipping on orders over 5,000 KZT",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(QodeCorners.sm),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                            ) {
                                Text(
                                    "NEW",
                                    modifier = Modifier.padding(horizontal = QodeSpacing.sm, vertical = QodeSpacing.xs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }
                    },
                    actions = {
                        QodeButton(
                            onClick = {},
                            text = "Copy Code",
                            variant = QodeButtonVariant.Text,
                            size = QodeButtonSize.Small,
                        )
                        QodeButton(
                            onClick = {},
                            text = "Use Now",
                            size = QodeButtonSize.Small,
                        )
                    },
                )

                QodeListCard(
                    title = "Notification Settings",
                    subtitle = "Get alerts for new promo codes",
                    leadingContent = {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                        )
                    },
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = QodeSpacing.md),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            // Color Palette
            SectionTitle("Brand Colors")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
            ) {
                ColorSwatch(
                    color = MaterialTheme.colorScheme.primary,
                    label = "Primary",
                    modifier = Modifier.weight(1f),
                )
                ColorSwatch(
                    color = MaterialTheme.colorScheme.secondary,
                    label = "Secondary",
                    modifier = Modifier.weight(1f),
                )
                ColorSwatch(
                    color = MaterialTheme.colorScheme.tertiary,
                    label = "Tertiary",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(QodeSpacing.xl))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = QodeSpacing.sm),
    )
}

@Composable
private fun ComponentGroup(
    title: String,
    content: @Composable RowScope.() -> Unit
) {
    Column(modifier = Modifier.padding(bottom = QodeSpacing.md)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = QodeSpacing.sm),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
            content = content,
        )
    }
}

@Composable
private fun ComponentColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        content = content,
    )
}

@Composable
private fun ColorSwatch(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            color = color,
            shape = RoundedCornerShape(QodeCorners.sm),
        ) {}
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = QodeSpacing.xs),
        )
    }
}
