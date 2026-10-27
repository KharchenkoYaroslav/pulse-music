package com.example.pulsemusic.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.pulsemusic.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SongDao {

    @Query("SELECT * FROM songs WHERE id = :id")
    abstract suspend fun getSongById(id: String): SongEntity?

    @Query("SELECT * FROM songs WHERE localAudioPath IS NOT NULL")
    abstract fun observeDownloadedSongs(): Flow<List<SongEntity>>

    @Query("DELETE FROM songs WHERE id = :id")
    abstract suspend fun deleteSong(id: String): Int

    @Query("UPDATE songs SET localAudioPath = NULL, localCoverPath = NULL WHERE id = :id")
    abstract suspend fun removeLocalPaths(id: String): Int

    @Transaction
    open suspend fun upsertSong(song: SongEntity): Long {
        val existing = getSongById(song.id)
        val mergedSong = if (existing != null) {
            song.copy(
                localAudioPath = song.localAudioPath ?: existing.localAudioPath,
                localCoverPath = song.localCoverPath ?: existing.localCoverPath,
                serverAudioUrl = song.serverAudioUrl ?: existing.serverAudioUrl,
                serverCoverUrl = song.serverCoverUrl ?: existing.serverCoverUrl,
                description = song.description ?: existing.description,
                authorLogin = song.authorLogin ?: existing.authorLogin,
                originalAuthor = song.originalAuthor ?: existing.originalAuthor
            )
        } else {
            song
        }
        return insertSong(mergedSong)
    }

    // Внутрішня функція
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertSong(song: SongEntity): Long
}
