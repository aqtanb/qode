package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.shared.model.Service
import kotlinx.coroutines.delay

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
    emptyMessage: String = "No services found"
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
    emptyMessage: String
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

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                modifier = Modifier.fillMaxWidth(),
            ) {
                popularServices.take(12).forEach { service ->
                    FilterChip(
                        selected = service.name == currentSelection,
                        onClick = { onServiceSelected(service) },
                        label = {
                            Text(
                                text = service.name,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                    )
                }
            }
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
                    items(popularServices.drop(12)) { service ->
                        ServiceItem(
                            service = service,
                            isSelected = service.name == currentSelection,
                            onClick = { onServiceSelected(service) },
                        )
                    }
                } else {
                    // Show search results
                    items(services) { service ->
                        ServiceItem(
                            service = service,
                            isSelected = service.name == currentSelection,
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
            modifier = Modifier.padding(SpacingTokens.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Column {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )

                if (service.category.isNotBlank()) {
                    Text(
                        text = service.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (service.promoCodeCount > 0) {
                    Text(
                        text = "${service.promoCodeCount} promo codes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}
