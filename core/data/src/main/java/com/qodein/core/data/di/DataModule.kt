package com.qodein.core.data.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.qodein.core.data.repository.AuthRepositoryImpl
import com.qodein.core.data.repository.BannerRepositoryImpl
import com.qodein.core.data.repository.DevicePreferencesRepositoryImpl
import com.qodein.core.data.repository.PromoCodeRepositoryImpl
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.domain.repository.PromoCodeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPromoCodeRepository(promoCodeRepositoryImpl: PromoCodeRepositoryImpl): PromoCodeRepository

    @Binds
    @Singleton
    abstract fun bindDevicePreferencesRepository(
        devicePreferencesRepositoryImpl: DevicePreferencesRepositoryImpl
    ): DevicePreferencesRepository

    @Binds
    @Singleton
    abstract fun bindBannerRepository(bannerRepositoryImpl: BannerRepositoryImpl): BannerRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore
    }
}
