package com.example.pulsemusic.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pulsemusic.core.database.dao.CategoryDao
import com.example.pulsemusic.core.database.dao.ProfileDao
import com.example.pulsemusic.core.database.dao.SongDao
import com.example.pulsemusic.core.database.entity.CategoryEntity
import com.example.pulsemusic.core.database.entity.SongEntity
import com.example.pulsemusic.core.database.entity.UserProfileEntity

// Локальна БД для збереження Пісень категорій та профілю
@Database(
    entities = [
        SongEntity::class,
        CategoryEntity::class,
        UserProfileEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun categoryDao(): CategoryDao
    abstract fun profileDao(): ProfileDao
}
