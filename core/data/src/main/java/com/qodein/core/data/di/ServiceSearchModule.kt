package com.qodein.core.data.di

import com.qodein.core.data.manager.ServiceSearchManagerImpl
import com.qodein.shared.domain.manager.ServiceSearchManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides ServiceSearchManager dependency for the application.
 *
 * Uses interface-implementation binding pattern for better testability
 * and loose coupling between domain and data layers.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceSearchModule {

    @Binds
    @Singleton
    abstract fun bindServiceSearchManager(serviceSearchManagerImpl: ServiceSearchManagerImpl): ServiceSearchManager
}
