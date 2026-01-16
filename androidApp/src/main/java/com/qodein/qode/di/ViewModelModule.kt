package com.qodein.qode.di

import com.qodein.qode.MainActivityViewModel
import com.qodein.qode.ui.QodeAppViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainActivityViewModel(get()) }
    viewModel { QodeAppViewModel(get()) }
}
