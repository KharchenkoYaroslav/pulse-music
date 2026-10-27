package com.example.pulsemusic.feature.library.data.model

data class SongHistoryItem(
    val id: String,
    val title: String,
    val cover: String?,
    val viewedAt: Long,
    val authorId: String,
    val authorLogin: String,
    val originalAuthor: String?
)
