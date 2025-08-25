package com.qodein.feature.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.home.R
import com.qodein.feature.home.model.CategoryFilter
import com.qodein.feature.home.model.PromoCodeTypeFilter
import com.qodein.feature.home.model.ServiceFilter
import com.qodein.feature.home.model.SortFilter
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.model.Service

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeFilterDialog(
    currentFilter: PromoCodeTypeFilter,
    onFilterSelected: (PromoCodeTypeFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier.padding(SpacingTokens.lg),
    ) {
        Card {
            Column(
                modifier = Modifier.padding(SpacingTokens.lg),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                Text(
                    text = stringResource(R.string.filter_dialog_type_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Column {
                    TypeFilterOption(
                        text = stringResource(R.string.filter_type_all),
                        isSelected = currentFilter is PromoCodeTypeFilter.All,
                        onClick = { onFilterSelected(PromoCodeTypeFilter.All) },
                    )

                    TypeFilterOption(
                        text = stringResource(R.string.filter_type_percentage),
                        isSelected = currentFilter is PromoCodeTypeFilter.Percentage,
                        onClick = { onFilterSelected(PromoCodeTypeFilter.Percentage) },
                    )

                    TypeFilterOption(
                        text = stringResource(R.string.filter_type_fixed_amount),
                        isSelected = currentFilter is PromoCodeTypeFilter.FixedAmount,
                        onClick = { onFilterSelected(PromoCodeTypeFilter.FixedAmount) },
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_close))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterDialog(
    currentFilter: CategoryFilter,
    onFilterSelected: (CategoryFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier.padding(SpacingTokens.lg),
    ) {
        Card {
            Column(
                modifier = Modifier.padding(SpacingTokens.lg),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                Text(
                    text = stringResource(R.string.filter_dialog_category_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                // All Categories option
                FilterChip(
                    selected = currentFilter is CategoryFilter.All,
                    onClick = { onFilterSelected(CategoryFilter.All) },
                    label = { Text(stringResource(R.string.filter_category_all)) },
                )

                HorizontalDivider()

                // Category grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    for (category in Service.Companion.Categories.ALL) {
                        val isSelected = currentFilter is CategoryFilter.Selected && currentFilter.category == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterSelected(CategoryFilter.Selected(category)) },
                            label = { Text(category) },
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_close))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceFilterDialog(
    currentFilter: ServiceFilter,
    availableServices: List<Service>,
    onFilterSelected: (ServiceFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    // Group services by category for hierarchical display
    val servicesByCategory = remember(searchQuery, availableServices) {
        val filtered = if (searchQuery.isBlank()) {
            availableServices
        } else {
            availableServices.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
            }
        }

        filtered
            .groupBy { it.category }
            .mapValues { (_, services) ->
                services.sortedWith(
                    compareByDescending<Service> { it.isPopular }
                        .thenByDescending { it.promoCodeCount }
                        .thenBy { it.name },
                )
            }
            .toSortedMap()
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier.padding(SpacingTokens.lg),
    ) {
        Card {
            Column(
                modifier = Modifier.padding(SpacingTokens.lg),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                Text(
                    text = stringResource(R.string.filter_dialog_service_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.filter_search_services)) },
                    leadingIcon = {
                        Icon(
                            QodeNavigationIcons.Search,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                // All Services option
                ServiceFilterOption(
                    service = null,
                    isSelected = currentFilter is ServiceFilter.All,
                    onClick = { onFilterSelected(ServiceFilter.All) },
                )

                HorizontalDivider()

                // Services list grouped by category
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    servicesByCategory.forEach { (category, services) ->
                        // Category header
                        item(key = "category_$category") {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(
                                    vertical = SpacingTokens.xs,
                                    horizontal = SpacingTokens.sm,
                                ),
                            )
                        }

                        // Services in this category
                        items(
                            items = services,
                            key = { service -> "service_${service.id.value}" },
                        ) { service ->
                            val isSelected = currentFilter is ServiceFilter.Selected && currentFilter.service.id == service.id
                            ServiceFilterOption(
                                service = service,
                                isSelected = isSelected,
                                onClick = { onFilterSelected(ServiceFilter.Selected(service)) },
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_close))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterDialog(
    currentFilter: SortFilter,
    onFilterSelected: (SortFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier.padding(SpacingTokens.lg),
    ) {
        Card {
            Column(
                modifier = Modifier.padding(SpacingTokens.lg),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                Text(
                    text = stringResource(R.string.filter_dialog_sort_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Column {
                    SortFilterOption(
                        sortBy = PromoCodeSortBy.POPULARITY,
                        text = stringResource(R.string.filter_sort_popularity),
                        isSelected = (currentFilter as SortFilter.Selected).sortBy == PromoCodeSortBy.POPULARITY,
                        onClick = { onFilterSelected(SortFilter.Selected(PromoCodeSortBy.POPULARITY)) },
                    )

                    SortFilterOption(
                        sortBy = PromoCodeSortBy.NEWEST,
                        text = stringResource(R.string.filter_sort_newest),
                        isSelected = (currentFilter as SortFilter.Selected).sortBy == PromoCodeSortBy.NEWEST,
                        onClick = { onFilterSelected(SortFilter.Selected(PromoCodeSortBy.NEWEST)) },
                    )

                    SortFilterOption(
                        sortBy = PromoCodeSortBy.EXPIRING_SOON,
                        text = stringResource(R.string.filter_sort_expiry_soon),
                        isSelected = (currentFilter as SortFilter.Selected).sortBy == PromoCodeSortBy.EXPIRING_SOON,
                        onClick = { onFilterSelected(SortFilter.Selected(PromoCodeSortBy.EXPIRING_SOON)) },
                    )

                    SortFilterOption(
                        sortBy = PromoCodeSortBy.ALPHABETICAL,
                        text = stringResource(R.string.filter_sort_alphabetical),
                        isSelected = (currentFilter as SortFilter.Selected).sortBy == PromoCodeSortBy.ALPHABETICAL,
                        onClick = { onFilterSelected(SortFilter.Selected(PromoCodeSortBy.ALPHABETICAL)) },
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_close))
                }
            }
        }
    }
}

@Composable
private fun TypeFilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ServiceFilterOption(
    service: Service?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service?.name ?: stringResource(R.string.filter_service_all),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                if (service != null && service.promoCodeCount > 0) {
                    Text(
                        text = "${service.promoCodeCount} codes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (service != null && service.isPopular) {
                Text(
                    text = "✨",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun SortFilterOption(
    sortBy: PromoCodeSortBy,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@Composable
private fun TypeFilterDialogPreview() {
    QodeTheme {
        TypeFilterDialog(
            currentFilter = PromoCodeTypeFilter.All,
            onFilterSelected = {},
            onDismiss = {},
        )
    }
}
