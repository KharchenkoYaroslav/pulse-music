package com.example.pulsemusic.feature.player.data.model

data class Song(
    val id: String,
    val title: String,
    val description: String?,
    val authorId: String,
    val authorLogin: String? = null,
    val categoryIds: List<String>,
    val likesCount: Int,
    val hasLiked: Boolean,
    val viewsCount: Int,
    val song: String?,
    val cover: String?,
    val originalAuthor: String? = null
)
