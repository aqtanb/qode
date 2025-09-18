package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.core.ui.preview.ServicePreviewData

@Composable
fun ServiceSelectionStep(
    serviceName: String,
    serviceLogoUrl: String? = null,
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
                    MaterialTheme.colorScheme.surfaceVariant
                },
                label = "backgroundColor",
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ShapeTokens.Corner.large))
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
                    if (isSelected && serviceLogoUrl != null) {
                        CircularImage(
                            imageUrl = serviceLogoUrl,
                            fallbackIcon = QodeCommerceIcons.Store,
                            contentDescription = "Service logo",
                            size = SizeTokens.Icon.sizeMedium,
                            modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        )
                    } else if (isSelected) {
                        Icon(
                            imageVector = QodeCommerceIcons.Store,
                            contentDescription = "Service",
                            modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        Icon(
                            imageVector = QodeNavigationIcons.Search,
                            contentDescription = "Search service",
                            modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = serviceName.ifBlank { stringResource(R.string.select_service_title) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
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
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                    )
                }
            }
        } else {
            QodeTextField(
                value = serviceName,
                onValueChange = onServiceNameChange,
                label = stringResource(R.string.service_name_label),
                placeholder = stringResource(R.string.service_name_placeholder),
                modifier = Modifier.fillMaxWidth(),
                required = true,
                leadingIcon = QodeActionIcons.Edit,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SpacingTokens.lg),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (!showManualEntry) {
                Text(
                    text = stringResource(R.string.cant_find_service_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.padding(SpacingTokens.xxxs))

                Text(
                    text = stringResource(R.string.type_manually_action),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onToggleManualEntry() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    text = stringResource(R.string.browse_services_action),
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
        ServiceSelectionStep(
            serviceName = ServicePreviewData.localCoffeeShop.name,
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
        ServiceSelectionStep(
            serviceName = ServicePreviewData.netflix.name,
            serviceLogoUrl = ServicePreviewData.netflix.logoUrl,
            showManualEntry = false,
            onServiceNameChange = {},
            onSelectService = {},
            onToggleManualEntry = {},
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}

@Preview(name = "Service Selection Card - No Logo", showBackground = true)
@Composable
private fun ServiceSelectionCardNoLogoPreview() {
    QodeTheme {
        ServiceSelectionStep(
            serviceName = ServicePreviewData.localCoffeeShop.name,
            serviceLogoUrl = ServicePreviewData.localCoffeeShop.logoUrl, // null
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
        ServiceSelectionStep(
            serviceName = ServicePreviewData.localCoffeeShop.name,
            showManualEntry = true,
            onServiceNameChange = {},
            onSelectService = {},
            onToggleManualEntry = {},
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}
