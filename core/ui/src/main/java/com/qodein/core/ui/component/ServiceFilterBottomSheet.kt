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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.shared.model.Service
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServiceSelectorBottomSheet(
    isVisible: Boolean,
    services: List<Service>,
    popularServices: List<Service>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onServiceSelected: (Service) -> Unit,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    sheetState: SheetState,
    selectedServices: List<Service> = emptyList(),
    onSearchFocused: (Boolean) -> Unit = {}
) {
    if (isVisible) {
        var isSearchFocused by remember { mutableStateOf(false) }

        SharedFilterBottomSheet(
            isVisible = isVisible,
            title = stringResource(R.string.select_service_title),
            onDismiss = onDismiss,
            sheetState = sheetState,
            modifier = modifier,
        ) {
            ServiceSelectorContent(
                services = services,
                popularServices = popularServices,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onServiceSelected = onServiceSelected,
                onSearch = onSearch,
                isLoading = isLoading,
                selectedServices = selectedServices,
                onDismiss = onDismiss,
                isSearchMode = false,
                onSearchFocusChange = { focused ->
                    isSearchFocused = focused
                    onSearchFocused(focused)
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceSelectorContent(
    services: List<Service>,
    popularServices: List<Service>,
    onServiceSelected: (Service) -> Unit,
    onSearch: (String) -> Unit,
    isLoading: Boolean,
    selectedServices: List<Service>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    isSearchMode: Boolean,
    onSearchFocusChange: ((Boolean) -> Unit)? = null
) {
    var isSearchFocused by remember { mutableStateOf(false) }
    val isSearching = searchQuery.length >= 2

    // Only show services for 2+ character searches
    val displayServices = if (isSearching) {
        services
    } else {
        emptyList()
    }

    // Sort popular services alphabetically
    val sortedPopularServices = remember(popularServices) {
        popularServices.sortedBy { it.name }
    }

    // Debounced search
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            delay(300)
            onSearch(searchQuery)
        }
    }

    Column(
        modifier = if (isSearchMode) Modifier.fillMaxWidth().padding(SpacingTokens.lg) else Modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        // Search mode header and back button
        if (isSearchMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = QodeActionIcons.Back,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
                Text(
                    text = stringResource(R.string.search_services_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Circular search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text(stringResource(R.string.search_services_label)) },
            placeholder = { Text(stringResource(R.string.search_services_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = QodeNavigationIcons.Search,
                    contentDescription = stringResource(R.string.search_icon_description),
                )
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = QodeActionIcons.Close,
                            contentDescription = stringResource(R.string.action_clear),
                        )
                    }
                }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isSearchFocused = focusState.isFocused
                    onSearchFocusChange?.invoke(focusState.isFocused)
                },

            shape = RoundedCornerShape(28.dp),
        )

        // Popular services chips (when search query is empty)
        if (searchQuery.isEmpty()) {
            Text(
                text = stringResource(R.string.popular_services_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val servicesToShow = sortedPopularServices

            if (servicesToShow.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    servicesToShow.take(20).forEach { service ->
                        ServiceChip(
                            service = service,
                            isSelected = selectedServices.any { it.id == service.id },
                            onClick = { onServiceSelected(service) },
                        )
                    }
                }
            } else if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.lg),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.size(SpacingTokens.sm))
                    Text(
                        text = stringResource(R.string.searching_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Search results (only when searching)
        if (isSearching) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                modifier = Modifier.height(if (isSearchMode) 600.dp else 400.dp),
            ) {
                if (isLoading) {
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
                                text = stringResource(R.string.searching_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    items(displayServices) { service ->
                        ServiceItem(
                            service = service,
                            isSelected = selectedServices.any { it.id == service.id },
                            onClick = { onServiceSelected(service) },
                        )
                    }

                    if (displayServices.isEmpty() && !isLoading) {
                        item {
                            Text(
                                text = stringResource(R.string.no_services_found_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(SpacingTokens.xl),
                            )
                        }
                    }
                }
            }
        }
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
                    promoCodeCount = 3,
                ),
                isSelected = false,
                onClick = {},
            )
        }
    }
}

@Composable
private fun ServiceChip(
    service: Service,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = service.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            )
        },
        leadingIcon = if (service.logoUrl != null) {
            {
                CircularImage(
                    imageUrl = service.logoUrl,
                    fallbackText = service.name,
                    fallbackIcon = QodeCommerceIcons.Store,
                    size = SizeTokens.Icon.sizeSmall,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    contentDescription = service.name,
                )
            }
        } else {
            null
        },
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
    )
}
