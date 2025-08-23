package com.qodein.core.data.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.qodein.core.data.repository.AuthRepositoryImpl
import com.qodein.core.data.repository.BannerRepositoryImpl
import com.qodein.core.data.repository.CommentRepositoryImpl
import com.qodein.core.data.repository.DevicePreferencesRepositoryImpl
import com.qodein.core.data.repository.PostRepositoryImpl
import com.qodein.core.data.repository.PromoCodeRepositoryImpl
import com.qodein.core.data.repository.PromoRepositoryImpl
import com.qodein.core.data.repository.UserInteractionRepositoryImpl
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.domain.repository.CommentRepository
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.domain.repository.PromoRepository
import com.qodein.shared.domain.repository.UserInteractionRepository
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

    @Binds
    @Singleton
    abstract fun bindPostRepository(postRepositoryImpl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindPromoRepository(promoRepositoryImpl: PromoRepositoryImpl): PromoRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(commentRepositoryImpl: CommentRepositoryImpl): CommentRepository

    @Binds
    @Singleton
    abstract fun bindUserInteractionRepository(userInteractionRepositoryImpl: UserInteractionRepositoryImpl): UserInteractionRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore
    }
}
