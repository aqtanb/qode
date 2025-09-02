package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

@Composable
fun ServiceSelectionCard(
    serviceName: String,
    showManualEntry: Boolean,
    onServiceNameChange: (String) -> Unit,
    onSelectService: () -> Unit,
    onToggleManualEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        if (!showManualEntry) {
            val isSelected = serviceName.isNotBlank()
            val interactionSource = remember { MutableInteractionSource() }

            val animatedBackgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                label = "backgroundColor",
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ShapeTokens.Corner.full))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(),
                        onClick = onSelectService,
                    ),
                color = animatedBackgroundColor,
                shape = RoundedCornerShape(ShapeTokens.Corner.large),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.lg),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = QodeNavigationIcons.Search,
                        contentDescription = null,
                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = serviceName.ifBlank { "Select Service" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = if (isSelected) "Selected" else "Browse or search",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Icon(
                        imageVector = QodeUIIcons.Breadcrumb,
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.lg),
            ) {
                QodeTextField(
                    value = serviceName,
                    onValueChange = onServiceNameChange,
                    label = "Service name",
                    placeholder = "Enter service name...",
                    modifier = Modifier.fillMaxWidth(),
                    required = true,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SpacingTokens.lg),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (!showManualEntry) {
                Text(
                    text = "Can't find it? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Type manually",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onToggleManualEntry() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    text = "Browse services instead",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onToggleManualEntry() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(name = "Service Selection Card - Empty", showBackground = true)
@Composable
private fun ServiceSelectionCardEmptyPreview() {
    QodeTheme {
        ServiceSelectionCard(
            serviceName = "",
            showManualEntry = false,
            onServiceNameChange = {},
            onSelectService = {},
            onToggleManualEntry = {},
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}

@Preview(name = "Service Selection Card - Selected", showBackground = true)
@Composable
private fun ServiceSelectionCardSelectedPreview() {
    QodeTheme {
        ServiceSelectionCard(
            serviceName = "Netflix",
            showManualEntry = false,
            onServiceNameChange = {},
            onSelectService = {},
            onToggleManualEntry = {},
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}

@Preview(name = "Service Selection Card - Manual Entry", showBackground = true)
@Composable
private fun ServiceSelectionCardManualPreview() {
    QodeTheme {
        ServiceSelectionCard(
            serviceName = "Custom Service",
            showManualEntry = true,
            onServiceNameChange = {},
            onSelectService = {},
            onToggleManualEntry = {},
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}
