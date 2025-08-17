package com.qodein.feature.promocode.submission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.Service

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceSelectorBottomSheet(
    isVisible: Boolean,
    services: List<Service>,
    currentSelection: String,
    onServiceSelected: (Service) -> Unit,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    isLoading: Boolean = false,
    sheetState: SheetState,
    modifier: Modifier = Modifier
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
                currentSelection = currentSelection,
                onServiceSelected = onServiceSelected,
                onSearch = onSearch,
                isLoading = isLoading,
            )
        }
    }
}

@Composable
private fun ServiceSelectorContent(
    services: List<Service>,
    currentSelection: String,
    onServiceSelected: (Service) -> Unit,
    onSearch: (String) -> Unit,
    isLoading: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        onSearch(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        // Header
        Text(
            text = "Select Service",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search services") },
            placeholder = { Text("Type to search...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        // Services list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            modifier = Modifier.height(300.dp),
        ) {
            items(services) { service ->
                ServiceItem(
                    service = service,
                    isSelected = service.name == currentSelection,
                    onClick = { onServiceSelected(service) },
                )
            }

            if (services.isEmpty() && !isLoading) {
                item {
                    Text(
                        text = "No services found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(SpacingTokens.xl),
                    )
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
            }
        }
    }
}
