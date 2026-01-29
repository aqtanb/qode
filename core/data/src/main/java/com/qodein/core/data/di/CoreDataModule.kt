package com.qodein.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkManager
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
import com.qodein.core.data.datasource.CacheMetadataDataSource
import com.qodein.core.data.datasource.DevicePreferencesDataSource
import com.qodein.core.data.datasource.FirebaseAuthDataSource
import com.qodein.core.data.datasource.FirebaseStorageDataSource
import com.qodein.core.data.datasource.FirestoreAppUpdateConfigDataSource
import com.qodein.core.data.datasource.FirestoreBannerDataSource
import com.qodein.core.data.datasource.FirestorePostDataSource
import com.qodein.core.data.datasource.FirestorePromocodeDataSource
import com.qodein.core.data.datasource.FirestoreReportDataSource
import com.qodein.core.data.datasource.FirestoreServiceDataSource
import com.qodein.core.data.datasource.FirestoreUserDataSource
import com.qodein.core.data.datasource.LocalReportDataSource
import com.qodein.core.data.post.WorkManagerPostSubmissionScheduler
import com.qodein.core.data.promocode.WorkManagerPromocodeSubmissionScheduler
import com.qodein.core.data.repository.AppUpdateConfigRepositoryImpl
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
import com.qodein.shared.domain.repository.AppUpdateConfigRepository
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.domain.repository.PostSubmissionScheduler
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.domain.repository.PromocodeSubmissionScheduler
import com.qodein.shared.domain.repository.ReportRepository
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.domain.repository.StorageRepository
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.domain.repository.UserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "device_preferences")
private val Context.reportDataStore: DataStore<Preferences> by preferencesDataStore(name = "report_preferences")
private val Context.cacheMetadataDataStore: DataStore<Preferences> by preferencesDataStore(name = "cache_metadata")

val coreDataModule = module {
    single<FirebaseAuth> { Firebase.auth }
    single<FirebaseFirestore> { Firebase.firestore }
    single<FirebaseFunctions> { Firebase.functions }
    single<FirebaseStorage> { Firebase.storage }
    single<DataStore<Preferences>> { androidContext().preferencesDataStore }
    single<WorkManager> { WorkManager.getInstance(androidContext()) }
    single {
        SearchClient(
            appId = BuildConfig.ALGOLIA_APP_ID,
            apiKey = BuildConfig.ALGOLIA_SEARCH_API_KEY,
        )
    }

    single { CacheMetadataDataSource(androidContext().cacheMetadataDataStore) }
    single { DevicePreferencesDataSource(get()) }
    single { FirebaseAuthDataSource(get()) }
    single { FirebaseStorageDataSource(get()) }
    single { FirestoreAppUpdateConfigDataSource(get()) }
    single { FirestoreBannerDataSource(get()) }
    single { FirestorePostDataSource(get()) }
    single { FirestorePromocodeDataSource(get()) }
    single { FirestoreServiceDataSource(get(), get()) }
    single { FirestoreUserDataSource(get(), get()) }
    single { FirestoreReportDataSource(get()) }
    single { LocalReportDataSource(androidContext().reportDataStore) }

    single<AppUpdateConfigRepository> { AppUpdateConfigRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<PromocodeRepository> { PromocodeRepositoryImpl(get()) }
    single<ServiceRepository> { ServiceRepositoryImpl(get(), get()) }
    single<DevicePreferencesRepository> { DevicePreferencesRepositoryImpl(androidContext(), get()) }
    single<BannerRepository> { BannerRepositoryImpl(get()) }
    single<UnifiedUserInteractionRepository> { UnifiedUserInteractionRepositoryImpl(get()) }
    single<PostRepository> { PostRepositoryImpl(get()) }
    single<StorageRepository> { StorageRepositoryImpl(get()) }
    single<ReportRepository> { ReportRepositoryImpl(get(), get()) }
    single<PostSubmissionScheduler> { WorkManagerPostSubmissionScheduler(get()) }
    single<PromocodeSubmissionScheduler> { WorkManagerPromocodeSubmissionScheduler(get()) }
}
