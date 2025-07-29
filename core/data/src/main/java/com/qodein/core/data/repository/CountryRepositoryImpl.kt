package com.qodein.core.data.repository

import android.content.Context
import com.qodein.core.data.mapper.toDomain
import com.qodein.core.domain.repository.CountryRepository
import com.qodein.core.model.Country
import com.simon.xmaterialccp.data.utils.getCountryName
import com.simon.xmaterialccp.data.utils.getDefaultLangCode
import com.simon.xmaterialccp.data.utils.getLibCountries
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) : CountryRepository {

    private val countries by lazy {
        getLibCountries().map { it.toDomain() }
    }

    private val localizedNames by lazy {
        countries.associate { country ->
            country.code to getLocalizedCountryName(country.code)
        }
    }

    override suspend fun getAllCountries(): List<Country> = countries

    override suspend fun getDefaultCountry(): Country {
        val deviceCountryCode = getDefaultLangCode(context)
        return countries.find { it.code == deviceCountryCode }
            ?: countries.first { it.code == "kz" }
    }

    override suspend fun searchCountries(query: String): List<Country> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return countries

        val queryLower = trimmedQuery.lowercase()

        return countries.filter { country ->
            // Prioritize exact matches first
            country.code.lowercase() == queryLower ||
                country.phoneCode.removePrefix("+") == queryLower.removePrefix("+") ||

                // Then partial matches
                localizedNames[country.code]?.lowercase()?.contains(queryLower) == true ||
                country.name.lowercase().contains(queryLower) ||
                country.phoneCode.contains(queryLower)
        }.sortedBy { country ->
            // Sort by relevance: exact matches first, then starts with, then contains
            when {
                country.code.lowercase() == queryLower -> 0
                country.phoneCode.removePrefix("+") == queryLower.removePrefix("+") -> 1
                localizedNames[country.code]?.lowercase()?.startsWith(queryLower) == true -> 2
                country.name.lowercase().startsWith(queryLower) -> 3
                else -> 4
            }
        }
    }

    private fun getLocalizedCountryName(countryCode: String): String =
        try {
            context.resources.getString(getCountryName(countryCode))
        } catch (e: Exception) {
            ""
        }
}
