package com.example.pulsemusic.feature.player.data

import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.core.data.LocalSongManager
import com.example.pulsemusic.core.data.model.UserPlaylist
import com.example.pulsemusic.feature.player.data.model.CheckSongInPlaylistsRequest
import com.example.pulsemusic.core.data.CoreApiService
import com.example.pulsemusic.feature.player.data.model.Song
import com.example.pulsemusic.feature.player.data.model.PlayerSource
import com.example.pulsemusic.core.network.BackendHostManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface PlayerRepository {
    fun checkIsUserLoggedIn(): Boolean
    suspend fun getSong(id: String, source: PlayerSource): Song?
    suspend fun recordView(id: String, source: PlayerSource = PlayerSource.EXTERNAL): Boolean
    suspend fun getUserPlaylists(): List<UserPlaylist>
    suspend fun checkSongInPlaylists(songId: String, playlistIds: List<String>): Map<String, Boolean>
    suspend fun addSongToPlaylist(playlistId: String, songId: String): Boolean
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Boolean
    suspend fun downloadSong(song: Song): Boolean
    suspend fun deleteSong(songId: String): Boolean
    suspend fun updateSong(song: Song): Boolean
}

// Репозиторій плеєра

class PlayerRepositoryImpl(
    private val playerApiService: PlayerApiService,
    private val coreApiService: CoreApiService,
    private val localSongManager: LocalSongManager,
    private val authPreferences: AuthPreferences,
    private val backendHostManager: BackendHostManager
) : PlayerRepository {
    
    private fun tokenProvider() = authPreferences.accessToken.value
    
    override fun checkIsUserLoggedIn(): Boolean = authPreferences.isLoggedIn()

    // Використовує як зовнішній, так і локальний ресурс та автоматично оновлює кеш
    override suspend fun getSong(id: String, source: PlayerSource): Song? {
        val localSong = localSongManager.getSongById(id)
        if (localSong != null) {
            if (source == PlayerSource.EXTERNAL) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val token = tokenProvider()?.let { "Bearer $it" }
                        val response = playerApiService.getSongDetails(token, id)
                        if (response.isSuccessful) {
                            response.body()?.let { serverSong ->
                                val updatedServerSong = serverSong.copy(
                                    cover = backendHostManager.buildFullUrl(serverSong.cover),
                                    song = backendHostManager.buildFullUrl(serverSong.song)
                                )
                                localSongManager.updateSong(updatedServerSong)
                            }
                        } else if (response.code() == 404) {
                            localSongManager.deleteSong(id)
                        }
                    } catch (_: Exception) {}
                }
            }
            return localSong
        }

        try {
            val token = tokenProvider()?.let { "Bearer $it" }
            val response = playerApiService.getSongDetails(token, id)
            if (response.isSuccessful) {
                response.body()?.let { serverSong ->
                    val updatedServerSong = serverSong.copy(
                        cover = backendHostManager.buildFullUrl(serverSong.cover),
                        song = backendHostManager.buildFullUrl(serverSong.song)
                    )

                    if (localSongManager.isSongDownloaded(id)) {
                        localSongManager.updateSong(updatedServerSong)
                        return localSongManager.getSongById(id) ?: updatedServerSong
                    }

                    return updatedServerSong
                }
            } else if (response.code() == 404) {
                localSongManager.deleteSong(id)
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return localSongManager.getSongById(id)
    }

    override suspend fun recordView(id: String, source: PlayerSource): Boolean {
        if (source == PlayerSource.LOCAL) return false

        try {
            val token = tokenProvider()?.let { "Bearer $it" } ?: return false
            val response = playerApiService.recordView(token, id)
            if (response.isSuccessful) {
                return response.body()?.success ?: false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    override suspend fun getUserPlaylists(): List<UserPlaylist> {
        try {
            val token = tokenProvider()?.let { "Bearer $it" } ?: return emptyList()
            val response = coreApiService.getUserPlaylists(token)
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return emptyList()
    }

    override suspend fun checkSongInPlaylists(songId: String, playlistIds: List<String>): Map<String, Boolean> {
        if (playlistIds.isEmpty()) return emptyMap()

        try {
            val token = tokenProvider()?.let { "Bearer $it" } ?: return emptyMap()
            val response = playerApiService.checkSongInPlaylists(token, songId, CheckSongInPlaylistsRequest(playlistIds))
            if (response.isSuccessful) {
                return response.body() ?: emptyMap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return emptyMap()
    }

    override suspend fun addSongToPlaylist(playlistId: String, songId: String): Boolean {
        try {
            val token = tokenProvider()?.let { "Bearer $it" } ?: return false
            val response = playerApiService.addSongToPlaylist(token, playlistId, songId)
            if (response.isSuccessful) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Boolean {
        try {
            val token = tokenProvider()?.let { "Bearer $it" } ?: return false
            val response = playerApiService.removeSongFromPlaylist(token, playlistId, songId)
            if (response.isSuccessful) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    override suspend fun downloadSong(song: Song): Boolean {
        return localSongManager.saveSong(song)
    }

    override suspend fun deleteSong(songId: String): Boolean {
        return localSongManager.deleteSong(songId)
    }

    override suspend fun updateSong(song: Song): Boolean {
        return localSongManager.updateSong(song)
    }
}
