package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.shared.model.Service
import kotlinx.coroutines.delay

// TODO: Make all Service items look the same, popular ones, and search ones, they should show logourl, name, and category, promocodeCount, write preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServiceSelectorBottomSheet(
    isVisible: Boolean,
    services: List<Service>,
    popularServices: List<Service>,
    currentSelection: String,
    onServiceSelected: (Service) -> Unit,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    isLoading: Boolean = false,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    title: String = "Select Service",
    searchPlaceholder: String = "Search services...",
    emptyMessage: String = "No services found",
    selectedServices: List<Service> = emptyList()
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = modifier,
        ) {
            ServiceSelectorContent(
                services = services,
                popularServices = popularServices,
                currentSelection = currentSelection,
                onServiceSelected = onServiceSelected,
                onSearch = onSearch,
                isLoading = isLoading,
                title = title,
                searchPlaceholder = searchPlaceholder,
                emptyMessage = emptyMessage,
                selectedServices = selectedServices,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceSelectorContent(
    services: List<Service>,
    popularServices: List<Service>,
    currentSelection: String,
    onServiceSelected: (Service) -> Unit,
    onSearch: (String) -> Unit,
    isLoading: Boolean,
    title: String,
    searchPlaceholder: String,
    emptyMessage: String,
    selectedServices: List<Service> = emptyList()
) {
    var searchQuery by remember { mutableStateOf("") }
    val showingPopular = searchQuery.length < 2
    val displayServices = if (showingPopular) popularServices else services

    // Debounced search
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            delay(500) // 500ms debounce
            onSearch(searchQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        // Header
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search services") },
            placeholder = { Text(searchPlaceholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        // Popular services as quick buttons (when not searching)
        if (showingPopular && popularServices.isNotEmpty()) {
            Text(
                text = "Popular Services",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Services list (search results or remaining popular services)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            modifier = Modifier.height(if (showingPopular && popularServices.isNotEmpty()) 300.dp else 400.dp),
        ) {
            if (isLoading && !showingPopular) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.xl),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.size(SpacingTokens.md))
                        Text(
                            text = "Loading services...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                if (showingPopular) {
                    // Show remaining popular services (after the chips)
                    items(popularServices) { service ->
                        ServiceItem(
                            service = service,
                            isSelected = selectedServices.any { it.id == service.id },
                            onClick = { onServiceSelected(service) },
                        )
                    }
                } else {
                    // Show search results
                    items(services) { service ->
                        ServiceItem(
                            service = service,
                            isSelected = selectedServices.any { it.id == service.id },
                            onClick = { onServiceSelected(service) },
                        )
                    }
                }

                if (displayServices.isEmpty() && !isLoading) {
                    item {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(SpacingTokens.xl),
                        )
                    }
                }
            }
        }

        // Bottom spacing
        Spacer(modifier = Modifier.height(SpacingTokens.lg))
    }
}

@Composable
private fun ServiceItem(
    service: Service,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeCard(
        onClick = onClick,
        variant = if (isSelected) QodeCardVariant.Filled else QodeCardVariant.Outlined,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            modifier = modifier.fillMaxWidth(),
        ) {
            CircularImage(
                imageUrl = service.logoUrl,
                fallbackText = service.name,
                fallbackIcon = QodeCommerceIcons.Store,
                size = SizeTokens.Icon.sizeMedium,
                backgroundColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                contentDescription = "stringResource(nameRes)",
            )

            Text(
                text = service.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(3f),
            )

            Text(
                text = service.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = "${service.promoCodeCount} codes",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// Preview for the ServiceItem
@Preview(showBackground = true)
@Composable
private fun ServiceItemPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Service Items Preview",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Popular service with logo and high promo count
            ServiceItem(
                service = Service.create(
                    name = "Netflix",
                    category = "Streaming",
                    logoUrl = "https://logo.url/netflix.png",
                    isPopular = true,
                    promoCodeCount = 25,
                ),
                isSelected = false,
                onClick = {},
            )

            // Selected service
            ServiceItem(
                service = Service.create(
                    name = "Uber Eats",
                    category = "Food",
                    logoUrl = null, // No logo - will show first letter
                    isPopular = false,
                    promoCodeCount = 12,
                ),
                isSelected = true,
                onClick = {},
            )

            // Service without promo codes
            ServiceItem(
                service = Service.create(
                    name = "Spotify Premium",
                    category = "Music",
                    logoUrl = "https://logo.url/spotify.png",
                    isPopular = true,
                    promoCodeCount = 0,
                ),
                isSelected = false,
                onClick = {},
            )

            // Long name service
            ServiceItem(
                service = Service.create(
                    name = "Super Long Service Name That Gets Truncated",
                    category = "Shopping",
                    logoUrl = null,
                    isPopular = false,
                    promoCodeCount = 156,
                ),
                isSelected = false,
                onClick = {},
            )

            // Minimal service
            ServiceItem(
                service = Service.create(
                    name = "Epic Games",
                    category = "Gaming",
                    logoUrl = null,
                    isPopular = false,
                    promoCodeCount = 3,
                ),
                isSelected = false,
                onClick = {},
            )
        }
    }
}
