@file:Suppress("unused")

package com.example.pulsemusic.di

import com.example.pulsemusic.core.data.CoreApiService
import com.example.pulsemusic.feature.auth.data.AuthApiService
import com.example.pulsemusic.feature.explore.data.ExploreApiService
import com.example.pulsemusic.feature.library.data.LibraryApiService
import com.example.pulsemusic.feature.player.data.PlayerApiService
import com.example.pulsemusic.feature.profile.data.ProfileApiService
import com.example.pulsemusic.feature.studio.data.StudioApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// Провайдер API сервісів додатку
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun providePlayerApiService(retrofit: Retrofit): PlayerApiService =
        retrofit.create(PlayerApiService::class.java)

    @Provides
    @Singleton
    fun provideCoreApiService(retrofit: Retrofit): CoreApiService =
        retrofit.create(CoreApiService::class.java)

    @Provides
    @Singleton
    fun provideExploreApiService(retrofit: Retrofit): ExploreApiService =
        retrofit.create(ExploreApiService::class.java)

    @Provides
    @Singleton
    fun provideLibraryApiService(retrofit: Retrofit): LibraryApiService =
        retrofit.create(LibraryApiService::class.java)

    @Provides
    @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService =
        retrofit.create(ProfileApiService::class.java)

    @Provides
    @Singleton
    fun provideStudioApiService(retrofit: Retrofit): StudioApiService =
        retrofit.create(StudioApiService::class.java)
}
