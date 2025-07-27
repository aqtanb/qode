package com.qodein.core.data.di

import com.qodein.core.data.repository.CountryRepositoryImpl
import com.qodein.core.domain.repository.CountryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindCountryRepository(countryRepositoryImpl: CountryRepositoryImpl): CountryRepository
}
