package com.qodein.core.common.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Dagger Hilt module for common utility dependencies.
 *
 * Currently empty as common utilities have been moved to appropriate modules.
 * This module is kept for future common dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule
