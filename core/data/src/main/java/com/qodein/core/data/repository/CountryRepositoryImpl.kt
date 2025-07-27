package com.qodein.core.data.repository

import android.content.Context
import com.qodein.core.data.mapper.toDomain
import com.qodein.core.domain.repository.CountryRepository
import com.qodein.core.model.Country
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

    override suspend fun getAllCountries(): List<Country> = countries

    override suspend fun getDefaultCountry(): Country {
        val deviceCountryCode = getDefaultLangCode(context)
        return countries.find { it.code == deviceCountryCode }
            ?: countries.first { it.code == "kz" }
    }

    override suspend fun searchCountries(query: String): List<Country> {
        if (query.isBlank()) return countries

        return countries.filter { country ->
            country.name.contains(query, ignoreCase = true) ||
                country.phoneCode.contains(query) ||
                country.code.contains(query, ignoreCase = true)
        }
    }
}
