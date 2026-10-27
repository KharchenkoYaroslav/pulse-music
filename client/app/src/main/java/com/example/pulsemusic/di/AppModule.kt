@file:Suppress("unused")

package com.example.pulsemusic.di

import android.content.Context
import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.feature.player.MusicPlayerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Провайдер загальних системних компонентів
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthPreferences(@ApplicationContext context: Context): AuthPreferences {
        return AuthPreferences(context)
    }

    @Provides
    @Singleton
    fun provideMusicPlayerManager(@ApplicationContext context: Context): MusicPlayerManager {
        return MusicPlayerManager(context)
    }
}
