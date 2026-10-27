package com.example.pulsemusic.feature.library.ui.tabs.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsemusic.core.data.LocalSongManager
import com.example.pulsemusic.feature.player.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Модель представлення завантажуваних
@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val localSongManager: LocalSongManager
) : ViewModel() {

    private val _downloadedSongs = MutableStateFlow<List<Song>>(emptyList())
    val downloadedSongs: StateFlow<List<Song>> = _downloadedSongs.asStateFlow()

    init {
        viewModelScope.launch {
            localSongManager.observeDownloadedSongs().collect { songs ->
                _downloadedSongs.update { songs }
            }
        }
    }

    fun deleteSong(id: String) {
        viewModelScope.launch { localSongManager.deleteSong(id) }
    }
}
