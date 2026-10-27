package com.example.pulsemusic.feature.studio.data.model

data class CreateExternalSong(
    val externalUrl: String,
    val categoryIds: List<String>? = null
)
