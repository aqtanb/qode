package com.qodein.shared.data.di

import com.qodein.shared.data.datasource.GithubMarkdownDataSource
import com.qodein.shared.data.repository.LegalDocumentRepositoryImpl
import com.qodein.shared.domain.repository.LegalDocumentRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import org.koin.dsl.module

val sharedDataModule = module {
    single {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 15000
            }
        }
    }
    single { GithubMarkdownDataSource(get()) }
    single<LegalDocumentRepository> { LegalDocumentRepositoryImpl(get()) }
}
