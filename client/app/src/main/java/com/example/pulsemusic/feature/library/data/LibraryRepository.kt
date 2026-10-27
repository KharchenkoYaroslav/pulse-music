package com.example.pulsemusic.feature.library.data

import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.data.model.UserPlaylist
import com.example.pulsemusic.core.data.CoreApiService
import com.example.pulsemusic.core.network.BackendHostManager
import com.example.pulsemusic.feature.library.data.model.CreatePlaylist
import com.example.pulsemusic.feature.library.data.model.SongHistoryItem
import com.example.pulsemusic.feature.library.data.model.UpdatePlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface LibraryRepository {
    suspend fun getHistoryCount(search: String? = null): Result<Int>
    suspend fun getLikesCount(search: String? = null): Result<Int>

    suspend fun getHistoryDirectly(from: Int, to: Int, search: String? = null): Result<List<SongHistoryItem>>
    suspend fun getLikesDirectly(from: Int, to: Int, search: String? = null): Result<List<SongRankingItem>>
    suspend fun getUserPlaylistsDirectly(search: String? = null): Result<List<UserPlaylist>>

    suspend fun createPlaylist(name: String): Result<String>
    suspend fun updatePlaylist(id: String, name: String): Result<Unit>
    suspend fun deletePlaylist(id: String): Result<Unit>
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit>
    suspend fun getPlaylistDetailsDirectly(id: String): Result<List<SongRankingItem>>
}

// Репозиторій бібліотеки
class LibraryRepositoryImpl(
    private val libraryApiService: LibraryApiService,
    private val coreApiService: CoreApiService,
    private val authPreferences: AuthPreferences,
    private val backendHostManager: BackendHostManager
) : LibraryRepository {

    private fun tokenProvider() = authPreferences.accessToken.value?.let { "Bearer $it" }

    override suspend fun getHistoryCount(search: String?): Result<Int> = withContext(Dispatchers.IO) {
        val token = tokenProvider() ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val response = libraryApiService.getHistoryCount(token, search)
            if (response.isSuccessful) {
                Result.success(response.body()?.count ?: 0)
            } else {
                Result.failure(Exception("Failed to get history count: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLikesCount(search: String?): Result<Int> = withContext(Dispatchers.IO) {
        val token = tokenProvider() ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val response = libraryApiService.getLikesCount(token, search)
            if (response.isSuccessful) {
                Result.success(response.body()?.count ?: 0)
            } else {
                Result.failure(Exception("Failed to get likes count: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserPlaylistsDirectly(search: String?): Result<List<UserPlaylist>> {
        val token = tokenProvider() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val response = coreApiService.getUserPlaylists(token, search)
            if (response.isSuccessful) {
                val playlists = response.body() ?: emptyList()
                Result.success(playlists)
            } else {
                Result.failure(Exception("Failed to load playlists: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlaylistDetailsDirectly(id: String): Result<List<SongRankingItem>> {
        val token = tokenProvider() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val response = libraryApiService.getPlaylistDetails(token, id)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Empty body"))
                val mappedSongs = body.songs.map { item ->
                    item.copy(cover = backendHostManager.buildFullUrl(item.cover))
                }
                Result.success(mappedSongs)
            } else {
                Result.failure(Exception("Failed to get playlist details: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHistoryDirectly(from: Int, to: Int, search: String?): Result<List<SongHistoryItem>> {
        val token = tokenProvider() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val response = libraryApiService.getHistory(token, from, to, search)
            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                val mapped = items.map { item ->
                    item.copy(cover = backendHostManager.buildFullUrl(item.cover))
                }
                Result.success(mapped)
            } else {
                Result.failure(Exception("Failed to load history directly: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLikesDirectly(from: Int, to: Int, search: String?): Result<List<SongRankingItem>> {
        val token = tokenProvider() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val response = libraryApiService.getLikes(token, from, to, search)
            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                val mapped = items.map { item ->
                    item.copy(cover = backendHostManager.buildFullUrl(item.cover))
                }
                Result.success(mapped)
            } else {
                Result.failure(Exception("Failed to load likes directly: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPlaylist(name: String): Result<String> {
        val token = tokenProvider() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val response = libraryApiService.createPlaylist(token, CreatePlaylist(name))
            if (response.isSuccessful) {
                val id = response.body()?.id ?: return Result.failure(Exception("No ID returned"))
                Result.success(id)
            } else {
                Result.failure(Exception("Failed to create playlist: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePlaylist(id: String, name: String): Result<Unit> {
        val token = tokenProvider() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val response = libraryApiService.updatePlaylist(token, id, UpdatePlaylist(name))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update playlist: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePlaylist(id: String): Result<Unit> {
        val token = tokenProvider() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val response = libraryApiService.deletePlaylist(token, id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete playlist: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit> {
        val token = tokenProvider() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val response = libraryApiService.removeSongFromPlaylist(token, playlistId, songId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove song from playlist: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}