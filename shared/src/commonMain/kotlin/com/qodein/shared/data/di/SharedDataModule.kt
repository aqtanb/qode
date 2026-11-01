package com.qodein.shared.data.di

import com.qodein.shared.data.datasource.GithubMarkdownDataSource
import com.qodein.shared.data.datasource.SupabaseAppConfigDataSource
import com.qodein.shared.data.datasource.SupabaseAuthDataSource
import com.qodein.shared.data.datasource.SupabaseBannerDataSource
import com.qodein.shared.data.datasource.SupabasePostDataSource
import com.qodein.shared.data.datasource.SupabasePromocodeDataSource
import com.qodein.shared.data.datasource.SupabaseReportDataSource
import com.qodein.shared.data.datasource.SupabaseServiceDataSource
import com.qodein.shared.data.datasource.SupabaseStorageDataSource
import com.qodein.shared.data.datasource.SupabaseUserDataSource
import com.qodein.shared.data.repository.AppUpdateConfigRepositoryImpl
import com.qodein.shared.data.repository.AuthRepositoryImpl
import com.qodein.shared.data.repository.BannerRepositoryImpl
import com.qodein.shared.data.repository.LegalDocumentRepositoryImpl
import com.qodein.shared.data.repository.PostRepositoryImpl
import com.qodein.shared.data.repository.PromocodeRepositoryImpl
import com.qodein.shared.data.repository.ServiceRepositoryImpl
import com.qodein.shared.data.repository.UnifiedUserInteractionRepositoryImpl
import com.qodein.shared.data.repository.UserRepositoryImpl
import com.qodein.shared.domain.repository.AppUpdateConfigRepository
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.domain.repository.LegalDocumentRepository
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

fun sharedDataModule(
    supabaseUrl: String,
    supabaseKey: String
): Module =
    module {
        single {
            HttpClient {
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

        single<SupabaseClient> {
            createSupabaseClient(supabaseUrl, supabaseKey) {
                install(Auth)
                install(Postgrest)
                install(Storage)
                install(Functions)
            }
        }
        single<Auth> { get<SupabaseClient>().auth }
        single<Postgrest> { get<SupabaseClient>().postgrest }
        single<Storage> { get<SupabaseClient>().storage }
        single<Functions> { get<SupabaseClient>().functions }

        single { GithubMarkdownDataSource(get()) }
        single { SupabaseAuthDataSource(get()) }
        single { SupabaseUserDataSource(get(), get(), get()) }
        single { SupabasePromocodeDataSource(get()) }
        single { SupabasePostDataSource(get()) }
        single { SupabaseServiceDataSource(get()) }
        single { SupabaseBannerDataSource(get()) }
        single { SupabaseAppConfigDataSource(get()) }
        single { SupabaseReportDataSource(get()) }
        single { SupabaseStorageDataSource(get(), get()) }

        single<LegalDocumentRepository> { LegalDocumentRepositoryImpl(get()) }
        single<AuthRepository> { AuthRepositoryImpl(get()) }
        single<UserRepository> { UserRepositoryImpl(get()) }
        single<UnifiedUserInteractionRepository> { UnifiedUserInteractionRepositoryImpl(get()) }
        single<PromocodeRepository> { PromocodeRepositoryImpl(get()) }
        single<PostRepository> { PostRepositoryImpl(get()) }
        single<ServiceRepository> { ServiceRepositoryImpl(get()) }
        single<BannerRepository> { BannerRepositoryImpl(get()) }
        single<AppUpdateConfigRepository> { AppUpdateConfigRepositoryImpl(get()) }
    }
