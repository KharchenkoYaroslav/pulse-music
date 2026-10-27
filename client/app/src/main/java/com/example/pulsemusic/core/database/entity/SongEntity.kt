package com.example.pulsemusic.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val authorId: String,
    val authorLogin: String?,
    val categoryIds: List<String>,
    val serverAudioUrl: String?,
    val serverCoverUrl: String?,
    val localAudioPath: String?,
    val localCoverPath: String?,
    val originalAuthor: String?
)
