package com.qodein.qode.di

import androidx.credentials.CredentialManager
import com.qodein.qode.auth.GoogleIdTokenProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { CredentialManager.create(androidContext()) }
    single { GoogleIdTokenProvider(get()) }
}
