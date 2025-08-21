package com.qodein.feature.promocode.submission.step1

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.TypeableDropdown
import com.qodein.core.ui.component.toDropdownItem
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.shared.model.Service

@Composable
fun ServiceAndTypeScreen(
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier,
    // These will be passed from the ViewModel in the next step
    availableServices: List<Service> = emptyList(),
    isLoadingServices: Boolean = false,
    onSearchServices: (String) -> Unit = {},
    shouldFocusService: Boolean = false
) {
    TrackScreenViewEvent(screenName = "SubmissionWizard_ServiceAndType")

    var searchQuery by remember { mutableStateOf(wizardData.serviceName) }

    // Trigger search when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            onSearchServices(searchQuery)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        // Service Selection
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)) {
            Text(
                text = "Service (Optional)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            TypeableDropdown(
                value = searchQuery,
                onValueChange = { newValue ->
                    searchQuery = newValue
                    // Update the wizard data when typing
                    onAction(SubmissionWizardAction.UpdateServiceName(newValue))
                },
                onItemSelected = { item ->
                    searchQuery = item.text
                    onAction(SubmissionWizardAction.UpdateServiceName(item.text))
                },
                items = availableServices.map { it.toDropdownItem() },
                label = "Service",
                placeholder = "Type to search services...",
                isLoading = isLoadingServices,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.md))

        // Discount Type Selection
        ModernTypeSelector(
            selectedType = wizardData.promoCodeType,
            onTypeSelected = { onAction(SubmissionWizardAction.UpdatePromoCodeType(it)) },
        )
    }
}

@Composable
private fun ModernTypeSelector(
    selectedType: PromoCodeType?,
    onTypeSelected: (PromoCodeType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg)) {
        Text(
            text = "Discount Type",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            ModernTypeCard(
                type = PromoCodeType.PERCENTAGE,
                title = "Percentage",
                icon = Icons.Default.Percent,
                isSelected = selectedType == PromoCodeType.PERCENTAGE,
                onClick = { onTypeSelected(PromoCodeType.PERCENTAGE) },
                modifier = Modifier.weight(1f),
            )

            ModernTypeCard(
                type = PromoCodeType.FIXED_AMOUNT,
                title = "Fixed Amount",
                icon = Icons.Default.AttachMoney,
                isSelected = selectedType == PromoCodeType.FIXED_AMOUNT,
                onClick = { onTypeSelected(PromoCodeType.FIXED_AMOUNT) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ModernTypeCard(
    type: PromoCodeType,
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "container_color",
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        animationSpec = tween(300),
        label = "border_color",
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "scale",
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) ShapeTokens.Border.medium else ShapeTokens.Border.thin,
            color = borderColor,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) SpacingTokens.sm else SpacingTokens.xs,
        ),
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            Box(
                modifier = Modifier
                    .size(SizeTokens.Avatar.sizeLarge)
                    .clip(RoundedCornerShape(ShapeTokens.Corner.extraLarge))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeXLarge),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

// MARK: - Enterprise-Level Previews

@Preview(name = "Service And Type Screen - Empty State", showBackground = true)
@Composable
private fun ServiceAndTypeScreenEmptyPreview() {
    QodeTheme {
        ServiceAndTypeScreen(
            wizardData = SubmissionWizardData(),
            onAction = {},
        )
    }
}

@Preview(name = "Service And Type Screen - Percentage Selected", showBackground = true)
@Composable
private fun ServiceAndTypeScreenPercentagePreview() {
    QodeTheme {
        ServiceAndTypeScreen(
            wizardData = SubmissionWizardData(
                serviceName = "Netflix",
                promoCodeType = PromoCodeType.PERCENTAGE,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Modern Type Card - Selected", showBackground = true)
@Composable
private fun ModernTypeCardSelectedPreview() {
    QodeTheme {
        ModernTypeCard(
            type = PromoCodeType.PERCENTAGE,
            title = "Percentage",
            icon = Icons.Default.Percent,
            isSelected = true,
            onClick = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
