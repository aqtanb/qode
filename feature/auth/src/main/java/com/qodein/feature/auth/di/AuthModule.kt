package com.qodein.feature.auth.di

import com.qodein.feature.auth.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    viewModel { AuthViewModel(get(), get(), get(), get()) }
}
