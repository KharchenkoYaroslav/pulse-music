@file:Suppress("unused")

package com.example.pulsemusic.di

import android.content.Context
import androidx.room.Room
import com.example.pulsemusic.core.database.AppDatabase
import com.example.pulsemusic.core.database.dao.CategoryDao
import com.example.pulsemusic.core.database.dao.ProfileDao
import com.example.pulsemusic.core.database.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Провайдер БД та її DAO
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mobilecoursework_db",
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideSongDao(database: AppDatabase): SongDao = database.songDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideProfileDao(database: AppDatabase): ProfileDao = database.profileDao()
}
