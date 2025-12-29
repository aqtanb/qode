package com.qodein.core.ui.di

import com.qodein.core.ui.refresh.ScreenRefreshCoordinator
import org.koin.dsl.module

val coreUiModule = module {
    single { ScreenRefreshCoordinator() }
}
