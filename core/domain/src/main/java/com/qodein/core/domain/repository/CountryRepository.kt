package com.qodein.core.domain.repository

import com.qodein.core.model.Country

interface CountryRepository {
    suspend fun getAllCountries(): List<Country>
    suspend fun getDefaultCountry(): Country
    suspend fun searchCountries(query: String): List<Country>
}
