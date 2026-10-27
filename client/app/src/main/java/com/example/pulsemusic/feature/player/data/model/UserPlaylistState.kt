package com.example.pulsemusic.feature.player.data.model

import com.example.pulsemusic.core.data.model.UserPlaylist

data class UserPlaylistState(
    val playlist: UserPlaylist,
    val isInPlaylist: Boolean
)
