package com.qodein.core.notifications.di

import com.qodein.core.notifications.Notifier
import com.qodein.core.notifications.SystemTrayNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    internal abstract fun bindNotifier(systemTrayNotifier: SystemTrayNotifier): Notifier
}
