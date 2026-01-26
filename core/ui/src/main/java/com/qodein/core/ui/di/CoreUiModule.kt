package com.qodein.core.ui.di

import com.qodein.core.ui.provider.AndroidShareStringProvider
import com.qodein.core.ui.refresh.ScreenRefreshCoordinator
import com.qodein.shared.domain.provider.ShareStringProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreUiModule = module {
    single { ScreenRefreshCoordinator() }
    single<ShareStringProvider> { AndroidShareStringProvider(androidContext()) }
}
