package com.example.pulsemusic.feature.library.data.model

import com.example.pulsemusic.core.data.model.SongRankingItem

data class PlaylistResponse(
    val id: String,
    val name: String,
    val userId: String,
    val userLogin: String,
    val songs: List<SongRankingItem>
)
