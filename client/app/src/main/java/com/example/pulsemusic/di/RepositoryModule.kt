@file:Suppress("unused")

package com.example.pulsemusic.di

import android.content.Context
import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.core.data.LocalSongManager
import com.example.pulsemusic.core.data.LocalSongManagerImpl
import com.example.pulsemusic.core.data.CoreRepository
import com.example.pulsemusic.core.data.CoreRepositoryImpl
import com.example.pulsemusic.core.network.BackendHostManager
import com.example.pulsemusic.core.data.CoreApiService
import com.example.pulsemusic.core.database.dao.CategoryDao
import com.example.pulsemusic.core.database.dao.ProfileDao
import com.example.pulsemusic.core.database.dao.SongDao
import com.example.pulsemusic.feature.auth.data.AuthApiService
import com.example.pulsemusic.feature.auth.data.AuthRepository
import com.example.pulsemusic.feature.auth.data.AuthRepositoryImpl
import com.example.pulsemusic.feature.explore.data.ExploreApiService
import com.example.pulsemusic.feature.explore.data.ExploreRepository
import com.example.pulsemusic.feature.explore.data.ExploreRepositoryImpl
import com.example.pulsemusic.feature.library.data.LibraryApiService
import com.example.pulsemusic.feature.library.data.LibraryRepository
import com.example.pulsemusic.feature.library.data.LibraryRepositoryImpl
import com.example.pulsemusic.feature.player.data.PlayerApiService
import com.example.pulsemusic.feature.player.data.PlayerRepository
import com.example.pulsemusic.feature.player.data.PlayerRepositoryImpl
import com.example.pulsemusic.feature.profile.data.ProfileApiService
import com.example.pulsemusic.feature.profile.data.ProfileRepository
import com.example.pulsemusic.feature.profile.data.ProfileRepositoryImpl
import com.example.pulsemusic.feature.studio.data.StudioApiService
import com.example.pulsemusic.feature.studio.data.StudioRepository
import com.example.pulsemusic.feature.studio.data.StudioRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Провайдер репозиторіїв
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCoreRepository(
        coreApiService: CoreApiService,
        authPreferences: AuthPreferences,
        categoryDao: CategoryDao
    ): CoreRepository {
        return CoreRepositoryImpl(coreApiService, authPreferences, categoryDao)
    }

    @Provides
    @Singleton
    fun provideLocalSongManager(
        @ApplicationContext context: Context,
        backendHostManager: BackendHostManager,
        songDao: SongDao,
        categoryDao: CategoryDao
    ): LocalSongManager {
        return LocalSongManagerImpl(context, backendHostManager, songDao)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: AuthApiService,
        authPrefs: AuthPreferences,
    ): AuthRepository {
        return AuthRepositoryImpl(apiService, authPrefs)
    }

    @Provides
    @Singleton
    fun providePlayerRepository(
        playerApiService: PlayerApiService,
        coreApiService: CoreApiService,
        localSongManager: LocalSongManager,
        authPreferences: AuthPreferences,
        backendHostManager: BackendHostManager
    ): PlayerRepository {
        return PlayerRepositoryImpl(
            playerApiService = playerApiService,
            coreApiService = coreApiService,
            localSongManager = localSongManager,
            authPreferences = authPreferences,
            backendHostManager = backendHostManager
        )
    }

    @Provides
    @Singleton
    fun provideLibraryRepository(
        libraryApiService: LibraryApiService,
        coreApiService: CoreApiService,
        authPreferences: AuthPreferences,
        backendHostManager: BackendHostManager
    ): LibraryRepository {
        return LibraryRepositoryImpl(
            libraryApiService, coreApiService, authPreferences, backendHostManager
        )
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        profileApiService: ProfileApiService,
        authPreferences: AuthPreferences,
        profileDao: ProfileDao
    ): ProfileRepository {
        return ProfileRepositoryImpl(profileApiService, authPreferences, profileDao)
    }

    @Provides
    @Singleton
    fun provideStudioRepository(
        studioApiService: StudioApiService,
        authPreferences: AuthPreferences,
    ): StudioRepository {
        return StudioRepositoryImpl(studioApiService, authPreferences)
    }

    @Provides
    @Singleton
    fun provideExploreRepository(
        exploreApiService: ExploreApiService,
        backendHostManager: BackendHostManager
    ): ExploreRepository {
        return ExploreRepositoryImpl(exploreApiService, backendHostManager)
    }
}
