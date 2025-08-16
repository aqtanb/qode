package com.qodein.feature.promocode.submission.step1

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.Service
import com.qodein.core.ui.component.TypeableDropdown
import com.qodein.core.ui.component.toDropdownItem
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData

@Composable
fun ServiceAndTypeScreen(
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier,
    // These will be passed from the ViewModel in the next step
    availableServices: List<Service> = emptyList(),
    isLoadingServices: Boolean = false,
    onSearchServices: (String) -> Unit = {}
) {
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
        // Hero Section with gradient background
        HeroSection()

        // Service Selection with TypeableDropdown
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
            Text(
                text = "Select Service",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
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

        // Promo Code Type Selection with modern cards
        PromoCodeTypeSelector(
            selectedType = wizardData.promoCodeType,
            onTypeSelected = { onAction(SubmissionWizardAction.UpdatePromoCodeType(it)) },
        )
    }
}

@Composable
private fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SpacingTokens.lg))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                    ),
                ),
            )
            .padding(SpacingTokens.lg),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            // Animated icon with glassmorphism effect
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(SpacingTokens.lg))
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = QodeCommerceIcons.PromoCode,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = "Create Promo Code",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Select your service and choose the discount type",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PromoCodeTypeSelector(
    selectedType: PromoCodeType?,
    onTypeSelected: (PromoCodeType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
        Text(
            text = "Discount Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            TypeCard(
                type = PromoCodeType.PERCENTAGE,
                title = "Percentage",
                subtitle = "% off total",
                icon = Icons.Default.Percent,
                isSelected = selectedType == PromoCodeType.PERCENTAGE,
                onClick = { onTypeSelected(PromoCodeType.PERCENTAGE) },
                modifier = Modifier.weight(1f),
            )

            TypeCard(
                type = PromoCodeType.FIXED_AMOUNT,
                title = "Fixed Amount",
                subtitle = "$ off total",
                icon = Icons.Default.AttachMoney,
                isSelected = selectedType == PromoCodeType.FIXED_AMOUNT,
                onClick = { onTypeSelected(PromoCodeType.FIXED_AMOUNT) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TypeCard(
    type: PromoCodeType,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .animateContentSize()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp,
        ),
        shape = RoundedCornerShape(SpacingTokens.lg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(SpacingTokens.md))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                },
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
