package com.example.pulsemusic.core.database.mapper

import com.example.pulsemusic.core.database.entity.SongEntity
import com.example.pulsemusic.feature.player.data.model.Song
import java.io.File

private fun resolveValidPath(localPath: String?, fallbackUrl: String?): String? {
    return if (!localPath.isNullOrEmpty() && File(localPath).exists()) {
        localPath
    } else {
        fallbackUrl
    }
}

fun SongEntity.toSong(): Song {
    return Song(
        id = id,
        title = title,
        description = description,
        authorId = authorId,
        authorLogin = authorLogin,
        categoryIds = categoryIds,
        likesCount = 0,
        hasLiked = false,
        viewsCount = 0,
        song = resolveValidPath(localAudioPath, serverAudioUrl),
        cover = resolveValidPath(localCoverPath, serverCoverUrl),
        originalAuthor = originalAuthor
    )
}

fun Song.toEntity(
    localAudioPath: String? = null,
    localCoverPath: String? = null,
    serverAudioUrl: String? = this.song,
    serverCoverUrl: String? = this.cover
): SongEntity {
    return SongEntity(
        id = id,
        title = title,
        description = description,
        authorId = authorId,
        authorLogin = authorLogin,
        categoryIds = categoryIds,
        serverAudioUrl = serverAudioUrl,
        serverCoverUrl = serverCoverUrl,
        localAudioPath = localAudioPath,
        localCoverPath = localCoverPath,
        originalAuthor = originalAuthor
    )
}
