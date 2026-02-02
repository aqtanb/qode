package com.qodein.core.notifications.di

import com.qodein.core.notifications.AndroidNotificationPermissionChecker
import com.qodein.core.notifications.Notifier
import com.qodein.core.notifications.SystemTrayNotifier
import com.qodein.shared.common.permission.NotificationPermissionChecker
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val notificationsModule = module {
    single<Notifier> { SystemTrayNotifier(androidContext()) }
    single<NotificationPermissionChecker> {
        AndroidNotificationPermissionChecker(androidContext())
    }
}
