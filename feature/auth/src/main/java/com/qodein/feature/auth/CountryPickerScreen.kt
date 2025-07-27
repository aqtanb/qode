package com.qodein.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.qodein.core.designsystem.component.QodeLoadingContent
import com.qodein.core.designsystem.component.SearchableTopAppBar
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.Country

@Composable
fun CountryPickerScreen(
    onBack: () -> Unit,
    onCountrySelected: (Country) -> Unit, // NEW - Pass selected country
    modifier: Modifier = Modifier,
    viewModel: CountryPickerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    CountryPickerContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::handleAction,
        onCountrySelected = { country ->
            onCountrySelected(country) // Pass it up - navigation handled in navigation layer
        },
        onBack = onBack,
    )
}

@Composable
fun CountryPickerContent(
    modifier: Modifier = Modifier,
    state: CountryPickerViewState,
    onAction: (CountryPickerAction) -> Unit,
    onCountrySelected: (Country) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Top app bar
        SearchableTopAppBar(
            title = stringResource(R.string.choose_a_country),
            isSearchActive = state.isSearchActive,
            searchQuery = state.searchQuery,
            onSearchQueryChange = { query ->
                onAction(CountryPickerAction.SearchCountries(query))
            },
            onBackClick = onBack,
            onSearchToggle = {
                onAction(CountryPickerAction.ToggleSearch)
            },
            searchPlaceholder = stringResource(R.string.search_placeholder),
            navigationContentDescription = stringResource(R.string.navigate_back),
            searchContentDescription = stringResource(R.string.search),
            clearSearchContentDescription = stringResource(R.string.clear_search),
        )

        // Content area
        when {
            state.isLoading -> {
                QodeLoadingContent()
            }
            state.filteredCountries.isEmpty() && state.searchQuery.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.no_countries_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                CountriesList(
                    countries = state.filteredCountries,
                    selectedCountry = state.selectedCountry,
                    onCountryClick = onCountrySelected,
                )
            }
        }
    }
}

@Composable
private fun CountriesList(
    countries: List<Country>,
    selectedCountry: Country?,
    onCountryClick: (Country) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(SpacingTokens.md),
    ) {
        items(
            items = countries,
            key = { it.code },
        ) { country ->
            CountryItem(
                country = country,
                isSelected = country.code == selectedCountry?.code,
                onClick = { onCountryClick(country) },
            )
        }
    }
}

@Composable
private fun CountryItem(
    country: Country,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Country flag
            Image(
                painter = painterResource(id = country.flagResourceId),
                contentDescription = "Flag of ${country.name}",
                modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
            )

            // Country info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    text = country.phoneCode,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = QodeActionIcons.Check,
                    contentDescription = stringResource(R.string.country_selected),
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
