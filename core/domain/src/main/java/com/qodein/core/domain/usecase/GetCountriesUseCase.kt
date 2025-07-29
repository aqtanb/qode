package com.qodein.core.domain.usecase

import com.qodein.core.domain.repository.CountryRepository
import com.qodein.core.model.Country
import javax.inject.Inject

class GetCountriesUseCase @Inject constructor(private val repository: CountryRepository) {
    suspend operator fun invoke(): List<Country> = repository.getAllCountries()
}
