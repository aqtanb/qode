package com.qodein.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.algolia.client.api.SearchClient
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.qodein.core.data.BuildConfig
import com.qodein.core.data.datasource.DevicePreferencesDataSource
import com.qodein.core.data.datasource.FirebaseAuthDataSource
import com.qodein.core.data.datasource.FirebaseStorageDataSource
import com.qodein.core.data.datasource.FirestoreBannerDataSource
import com.qodein.core.data.datasource.FirestorePostDataSource
import com.qodein.core.data.datasource.FirestorePromocodeDataSource
import com.qodein.core.data.datasource.FirestoreReportDataSource
import com.qodein.core.data.datasource.FirestoreServiceDataSource
import com.qodein.core.data.datasource.FirestoreUnifiedUserInteractionDataSource
import com.qodein.core.data.datasource.FirestoreUserDataSource
import com.qodein.core.data.datasource.LocalReportDataSource
import com.qodein.core.data.manager.ServiceSelectionManagerImpl
import com.qodein.core.data.mapper.UserInteractionMapper
import com.qodein.core.data.repository.AuthRepositoryImpl
import com.qodein.core.data.repository.BannerRepositoryImpl
import com.qodein.core.data.repository.DevicePreferencesRepositoryImpl
import com.qodein.core.data.repository.PostRepositoryImpl
import com.qodein.core.data.repository.PromocodeRepositoryImpl
import com.qodein.core.data.repository.ReportRepositoryImpl
import com.qodein.core.data.repository.ServiceRepositoryImpl
import com.qodein.core.data.repository.StorageRepositoryImpl
import com.qodein.core.data.repository.UnifiedUserInteractionRepositoryImpl
import com.qodein.core.data.repository.UserRepositoryImpl
import com.qodein.shared.domain.coordinator.ServiceSelectionCoordinator
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.domain.repository.ReportRepository
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.domain.repository.StorageRepository
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.domain.service.selection.ServiceSelectionManager
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "device_preferences")
private val Context.reportDataStore: DataStore<Preferences> by preferencesDataStore(name = "report_preferences")

val coreDataModule = module {
    single<FirebaseAuth> { Firebase.auth }
    single<FirebaseFirestore> { Firebase.firestore }
    single<FirebaseFunctions> { Firebase.functions }
    single<FirebaseStorage> { Firebase.storage }
    single<DataStore<Preferences>> { androidContext().preferencesDataStore }
    single { UserInteractionMapper() }
    single {
        SearchClient(
            appId = BuildConfig.ALGOLIA_APP_ID,
            apiKey = BuildConfig.ALGOLIA_SEARCH_API_KEY,
        )
    }

    single { DevicePreferencesDataSource(get()) }
    single { FirebaseAuthDataSource(get()) }
    single { FirebaseStorageDataSource(get()) }
    single { FirestoreBannerDataSource(get()) }
    single { FirestorePostDataSource(get()) }
    single { FirestorePromocodeDataSource(get()) }
    single { FirestoreServiceDataSource(get(), get()) }
    single { FirestoreUnifiedUserInteractionDataSource(get()) }
    single { FirestoreUserDataSource(get(), get()) }
    single { FirestoreReportDataSource(get()) }
    single { LocalReportDataSource(androidContext().reportDataStore) }

    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<PromocodeRepository> { PromocodeRepositoryImpl(get()) }
    single<ServiceRepository> { ServiceRepositoryImpl(get()) }
    single<DevicePreferencesRepository> { DevicePreferencesRepositoryImpl(androidContext(), get()) }
    single<BannerRepository> { BannerRepositoryImpl(get()) }
    single<UnifiedUserInteractionRepository> { UnifiedUserInteractionRepositoryImpl(get(), get()) }
    single<PostRepository> { PostRepositoryImpl(get()) }
    single<StorageRepository> { StorageRepositoryImpl(get()) }
    single<ReportRepository> { ReportRepositoryImpl(get(), get()) }

    single<ServiceSelectionManager> { ServiceSelectionManagerImpl() }
    single { ServiceSelectionCoordinator(get<SearchServicesUseCase>(), get()) }
}
