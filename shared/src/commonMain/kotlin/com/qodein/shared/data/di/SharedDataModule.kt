package com.qodein.shared.data.di

import com.qodein.shared.data.datasource.GithubMarkdownDataSource
import com.qodein.shared.data.repository.LegalDocumentRepositoryImpl
import com.qodein.shared.domain.repository.LegalDocumentRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val sharedDataModule = module {
    single {
        HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 15000
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }
        }
    }

    single { GithubMarkdownDataSource(get()) }

    single<LegalDocumentRepository> { LegalDocumentRepositoryImpl(get()) }
}
