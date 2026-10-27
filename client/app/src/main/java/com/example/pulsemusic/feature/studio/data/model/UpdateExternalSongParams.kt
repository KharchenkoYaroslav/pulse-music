package com.example.pulsemusic.feature.studio.data.model

data class UpdateExternalSongParams(
    val externalUrl: String? = null,
    val categoryIds: List<String>? = null
)
