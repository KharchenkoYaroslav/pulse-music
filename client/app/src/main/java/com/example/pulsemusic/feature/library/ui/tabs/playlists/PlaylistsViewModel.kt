package com.example.pulsemusic.feature.library.ui.tabs.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.data.model.UserPlaylist
import com.example.pulsemusic.feature.library.data.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistsUiState(
    val playlists: List<UserPlaylist> = emptyList(),
    val playlistDetails: Map<String, List<SongRankingItem>> = emptyMap(),
    val loadingPlaylists: Set<String> = emptySet(),
    val isLoading: Boolean = false
) {
    fun filteredPlaylists(query: String): List<UserPlaylist> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return playlists
        return playlists.filter { it.name.lowercase().contains(q) || playlistDetails[it.id]?.any { song -> song.title.lowercase().contains(q) } == true }
    }

    fun filteredDetails(query: String): Map<String, List<SongRankingItem>> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return playlistDetails
        return playlistDetails.mapValues { (_, songs) -> songs.filter { it.title.lowercase().contains(q) } }
    }
}

// Модель представлення плейлістів
@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadPlaylists(searchQuery: String) {
        if (authPreferences.accessToken.value.isNullOrBlank()) return
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }

        loadJob = viewModelScope.launch {
            val query = searchQuery.trim().takeIf { it.isNotEmpty() }
            val result = repository.getUserPlaylistsDirectly(search = query)
            _uiState.update { it.copy(playlists = result.getOrDefault(emptyList()), isLoading = false) }
        }
    }

    fun loadPlaylistDetails(playlistId: String) {
        if (_uiState.value.loadingPlaylists.contains(playlistId)) return
        _uiState.update { it.copy(loadingPlaylists = it.loadingPlaylists + playlistId) }

        viewModelScope.launch {
            val result = repository.getPlaylistDetailsDirectly(playlistId)
            if (result.isSuccess) {
                val songs = result.getOrDefault(emptyList())
                _uiState.update { it.copy(playlistDetails = it.playlistDetails + (playlistId to songs)) }
            }
            _uiState.update { it.copy(loadingPlaylists = it.loadingPlaylists - playlistId) }
        }
    }

    fun playAll(playlistId: String, onSongsReady: (List<String>) -> Unit) {
        viewModelScope.launch {
            val cachedSongs = _uiState.value.playlistDetails[playlistId]
            if (cachedSongs != null) {
                onSongsReady(cachedSongs.map { it.id })
                return@launch
            }

            _uiState.update { it.copy(loadingPlaylists = it.loadingPlaylists + playlistId) }
            val result = repository.getPlaylistDetailsDirectly(playlistId)
            if (result.isSuccess) {
                val songs = result.getOrDefault(emptyList())
                _uiState.update { it.copy(playlistDetails = it.playlistDetails + (playlistId to songs)) }
                onSongsReady(songs.map { it.id })
            }
            _uiState.update { it.copy(loadingPlaylists = it.loadingPlaylists - playlistId) }
        }
    }

    fun createPlaylist(name: String, searchQuery: String) {
        viewModelScope.launch {
            if (repository.createPlaylist(name).isSuccess) loadPlaylists(searchQuery)
        }
    }

    fun renamePlaylist(id: String, newName: String, searchQuery: String) {
        viewModelScope.launch {
            if (repository.updatePlaylist(id, newName).isSuccess) loadPlaylists(searchQuery)
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            if (repository.deletePlaylist(id).isSuccess) {
                _uiState.update { it.copy(playlists = it.playlists.filter { p -> p.id != id }, playlistDetails = it.playlistDetails - id) }
            }
        }
    }

    fun removeSong(playlistId: String, songId: String) {
        viewModelScope.launch {
            if (repository.removeSongFromPlaylist(playlistId, songId).isSuccess) {
                _uiState.update { state ->
                    val songs = state.playlistDetails[playlistId]?.filter { it.id != songId } ?: emptyList()
                    state.copy(playlistDetails = state.playlistDetails + (playlistId to songs))
                }
            }
        }
    }
}
