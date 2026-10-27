package com.example.pulsemusic.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: String = "CURRENT_USER",
    val login: String,
    val createdAt: String
)
