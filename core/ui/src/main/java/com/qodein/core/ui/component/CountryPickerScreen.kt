package com.qodein.core.ui.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.simon.xmaterialccp.data.CountryData
import com.simon.xmaterialccp.data.utils.getFlags
import com.simon.xmaterialccp.data.utils.getLibCountries
import com.simon.xmaterialccp.utils.searchCountry

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerScreen(
    selectedCountry: CountryData?,
    onCountrySelected: (CountryData) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val countries = remember { getLibCountries() }

    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            countries
        } else {
            countries.searchCountry(searchQuery, context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.select_country),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = QodeActionIcons.Back,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = SpacingTokens.screenPadding),
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(text = stringResource(R.string.search_countries))
                },
                leadingIcon = {
                    Icon(
                        imageVector = QodeNavigationIcons.Search,
                        contentDescription = null,
                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = QodeActionIcons.Close,
                                contentDescription = stringResource(R.string.clear_search),
                                modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(ShapeTokens.Corner.medium),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SpacingTokens.md),
            )

            // Countries list
            if (filteredCountries.isEmpty()) {
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    items(filteredCountries) { country ->
                        CountryItem(
                            country = country,
                            isSelected = selectedCountry?.countryCode == country.countryCode,
                            onClick = { onCountrySelected(country) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryItem(
    country: CountryData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(SpacingTokens.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Country flag
        Image(
            painter = painterResource(id = getFlags(country.countryCode)),
            contentDescription = stringResource(
                R.string.phone_input_flag_content_desc,
                country.cNames,
            ),
            modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
        )

        // Country info
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = country.cNames,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = country.countryPhoneCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Selection indicator
        if (isSelected) {
            Icon(
                imageVector = QodeStatusIcons.Verified,
                contentDescription = stringResource(R.string.country_selected),
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(name = "Country Picker Screen", showBackground = true)
@Composable
private fun CountryPickerScreenPreview() {
    val sampleCountries = listOf(
        CountryData("kz", "+7", "Kazakhstan"),
        CountryData("us", "+1", "United States"),
        CountryData("ru", "+7", "Russia"),
    )

    QodeTheme {
        CountryPickerScreen(
            selectedCountry = sampleCountries.first(),
            onCountrySelected = {},
            onBackPressed = {},
        )
    }
}
