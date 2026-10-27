package com.example.pulsemusic.core.data.model

data class SongRankingItem(
    val id: String,
    val title: String,
    val cover: String?,
    val authorId: String,
    val authorLogin: String,
    val originalAuthor: String?
)
