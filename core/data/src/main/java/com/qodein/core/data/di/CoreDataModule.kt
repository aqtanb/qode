package com.qodein.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.qodein.core.data.cache.QueryCache
import com.qodein.core.data.coordinator.ServiceSelectionCoordinator
import com.qodein.core.data.datasource.DevicePreferencesDataSource
import com.qodein.core.data.datasource.FirebaseGoogleAuthService
import com.qodein.core.data.datasource.FirebaseStorageDataSource
import com.qodein.core.data.datasource.FirestoreBannerDataSource
import com.qodein.core.data.datasource.FirestorePostDataSource
import com.qodein.core.data.datasource.FirestorePromocodeDataSource
import com.qodein.core.data.datasource.FirestoreServiceDataSource
import com.qodein.core.data.datasource.FirestoreUnifiedUserInteractionDataSource
import com.qodein.core.data.datasource.FirestoreUserDataSource
import com.qodein.core.data.manager.ServiceSearchManagerImpl
import com.qodein.core.data.manager.ServiceSelectionManagerImpl
import com.qodein.core.data.mapper.UserInteractionMapper
import com.qodein.core.data.repository.AuthRepositoryImpl
import com.qodein.core.data.repository.BannerRepositoryImpl
import com.qodein.core.data.repository.DevicePreferencesRepositoryImpl
import com.qodein.core.data.repository.PostRepositoryImpl
import com.qodein.core.data.repository.PromocodeRepositoryImpl
import com.qodein.core.data.repository.StorageRepositoryImpl
import com.qodein.core.data.repository.UnifiedUserInteractionRepositoryImpl
import com.qodein.core.data.repository.UserRepositoryImpl
import com.qodein.shared.domain.auth.AuthStateManager
import com.qodein.shared.domain.manager.ServiceSearchManager
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.domain.repository.StorageRepository
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.domain.service.ServiceCache
import com.qodein.shared.domain.service.selection.ServiceSelectionManager
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import com.qodein.shared.domain.usecase.banner.GetBannersUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserBookmarksUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleBookmarkUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.post.GetPostByIdUseCase
import com.qodein.shared.domain.usecase.post.GetPostsUseCase
import com.qodein.shared.domain.usecase.post.SubmitPostUseCase
import com.qodein.shared.domain.usecase.preferences.GetThemeUseCase
import com.qodein.shared.domain.usecase.preferences.ObserveLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.SetLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.SetThemeUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeByIdUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesUseCase
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "device_preferences")

val coreDataModule = module {
    single<FirebaseAuth> { Firebase.auth }
    single<FirebaseFirestore> { Firebase.firestore }
    single<FirebaseFunctions> { Firebase.functions }
    single<FirebaseStorage> { Firebase.storage }
    single<DataStore<Preferences>> { androidContext().dataStore }
    single { QueryCache() }
    single { UserInteractionMapper() }

    single { DevicePreferencesDataSource(get()) }
    single { FirebaseGoogleAuthService(get()) }
    single { FirebaseStorageDataSource(get()) }
    single { FirestoreBannerDataSource(get()) }
    single { FirestorePostDataSource(get()) }
    single { FirestorePromocodeDataSource(get(), get()) }
    single { FirestoreServiceDataSource(get()) }
    single { FirestoreUnifiedUserInteractionDataSource(get()) }
    single { FirestoreUserDataSource(get()) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<PromocodeRepository> { PromocodeRepositoryImpl(get(), get(), get()) }
    single<DevicePreferencesRepository> { DevicePreferencesRepositoryImpl(get()) }
    single<BannerRepository> { BannerRepositoryImpl(get()) }
    single<UnifiedUserInteractionRepository> { UnifiedUserInteractionRepositoryImpl(get(), get()) }
    single<PostRepository> { PostRepositoryImpl(get(), get()) }
    single<StorageRepository> { StorageRepositoryImpl(get()) }

    single<ServiceSearchManager> { ServiceSearchManagerImpl(get(), get(), get()) }
    single<ServiceSelectionManager> { ServiceSelectionManagerImpl() }
    single { ServiceCache.getInstance() }
    single { ServiceSelectionCoordinator(get<ServiceSearchManager>(), get<ServiceSelectionManager>(), get()) }

    single { AuthStateManager(get()) }
    single { GetAuthStateUseCase(get()) }
    single { SignOutUseCase(get()) }
    single { GetBannersUseCase(get()) }
    single { GetThemeUseCase(get()) }
    single { ObserveLanguageUseCase(get()) }
    single { SetThemeUseCase(get()) }
    single { SetLanguageUseCase(get()) }
    single { GetPromocodesUseCase(get()) }
    single { GetPromocodeByIdUseCase(get()) }
    single { SubmitPromocodeUseCase(get()) }
    single { ToggleVoteUseCase(get()) }
    single { ToggleBookmarkUseCase(get()) }
    single { GetUserBookmarksUseCase(get()) }
    single { GetUserInteractionUseCase(get()) }
    single { GetPopularServicesUseCase(get()) }
    single { SearchServicesUseCase(get()) }
    single { GetPostsUseCase(get()) }
    single { GetPostByIdUseCase(get()) }
    single { SubmitPostUseCase(get(), get()) }
    single { GetUserByIdUseCase(get()) }
}
