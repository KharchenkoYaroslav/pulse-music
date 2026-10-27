package com.example.pulsemusic.feature.library.ui.tabs.mysongs

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.core.data.CoreRepository
import com.example.pulsemusic.core.data.LocalSongManager
import com.example.pulsemusic.core.data.model.CategoryListResponse
import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.database.dao.SongDao
import com.example.pulsemusic.core.util.AppToastManager
import com.example.pulsemusic.feature.explore.data.ExploreRepository
import com.example.pulsemusic.feature.studio.data.StudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class MySongsUiState(
    val songs: List<SongRankingItem> = emptyList(),
    val categories: List<CategoryListResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

// Модель представлення моїх пісень
@HiltViewModel
class MySongsViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val studioRepository: StudioRepository,
    private val coreRepository: CoreRepository,
    private val authPreferences: AuthPreferences,
    private val localSongManager: LocalSongManager,
    private val songDao: SongDao,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MySongsUiState())
    val uiState: StateFlow<MySongsUiState> = _uiState.asStateFlow()

    private var hasMore = true
    private var offset = 0
    private var loadJob: Job? = null

    init {
        viewModelScope.launch { coreRepository.refreshCategories() }
        viewModelScope.launch {
            coreRepository.observeCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun loadInitial(searchQuery: String, pageSize: Int) {
        val userId = authPreferences.userId.value ?: return
        if (authPreferences.accessToken.value.isNullOrBlank()) return

        loadJob?.cancel()
        hasMore = true
        offset = 0
        _uiState.update { it.copy(isLoading = true) }

        loadJob = viewModelScope.launch {
            val query = searchQuery.trim().takeIf { it.isNotEmpty() }
            val result = exploreRepository.getTrendingByViewsDirectly(0, pageSize, authorId = userId, search = query)
            val items = result.getOrDefault(emptyList())

            hasMore = items.size >= pageSize
            offset = pageSize
            _uiState.update { it.copy(songs = items, isLoading = false, isError = !result.isSuccess) }
        }
    }

    fun loadMore(searchQuery: String, pageSize: Int) {
        val userId = authPreferences.userId.value ?: return
        if (_uiState.value.isLoading || !hasMore || authPreferences.accessToken.value.isNullOrBlank()) return

        _uiState.update { it.copy(isLoading = true) }
        loadJob = viewModelScope.launch {
            val query = searchQuery.trim().takeIf { it.isNotEmpty() }
            val result = exploreRepository.getTrendingByViewsDirectly(offset, offset + pageSize, authorId = userId, search = query)
            val newItems = result.getOrDefault(emptyList())

            hasMore = newItems.size >= pageSize
            val current = _uiState.value.songs
            val uniqueNew = newItems.filter { n -> current.none { it.id == n.id } }

            _uiState.update { it.copy(songs = current + uniqueNew, isLoading = false) }
            offset += pageSize
        }
    }

    fun createLocalSong(title: String, desc: String?, origAuth: String?, cats: List<String>?, songUri: Uri?, coverUri: Uri?, query: String, size: Int) {
        viewModelScope.launch {
            val songF = songUri?.let { uriToFile(it) }
            val coverF = coverUri?.let { uriToFile(it) }
            try {
                val res = studioRepository.createLocalSong(title, desc, origAuth, cats, songF, coverF)
                if (res.isSuccess) { AppToastManager.showMessage("Створено"); loadInitial(query, size) }
                else AppToastManager.showMessage(res.exceptionOrNull()?.message ?: "Помилка")
            } finally {
                songF?.delete()
                coverF?.delete()
            }
        }
    }

    fun createExternalSong(url: String, cats: List<String>?, query: String, size: Int) {
        viewModelScope.launch {
            val res = studioRepository.createExternalSong(url, cats)
            if (res.isSuccess) { AppToastManager.showMessage("Додано"); loadInitial(query, size) }
            else AppToastManager.showMessage(res.exceptionOrNull()?.message ?: "Помилка")
        }
    }

    fun updateLocalSong(id: String, title: String?, desc: String?, origAuth: String?, cats: List<String>?, songUri: Uri?, coverUri: Uri?, query: String, size: Int) {
        viewModelScope.launch {
            val songF = songUri?.let { uriToFile(it) }
            val coverF = coverUri?.let { uriToFile(it) }
            try {
                val res = studioRepository.updateLocalSong(id, title, desc, origAuth, cats, songF, coverF)
                if (res.isSuccess) { AppToastManager.showMessage("Оновлено"); loadInitial(query, size) }
                else AppToastManager.showMessage(res.exceptionOrNull()?.message ?: "Помилка")
            } finally {
                songF?.delete()
                coverF?.delete()
            }
        }
    }

    fun updateExternalSong(id: String, url: String?, cats: List<String>?, query: String, size: Int) {
        viewModelScope.launch {
            val res = studioRepository.updateExternalSong(id, url, cats)
            if (res.isSuccess) { AppToastManager.showMessage("Оновлено"); loadInitial(query, size) }
            else AppToastManager.showMessage(res.exceptionOrNull()?.message ?: "Помилка")
        }
    }

    fun deleteSong(id: String, isExternal: Boolean = false) {
        viewModelScope.launch {
            val res = if (isExternal) studioRepository.deleteExternalSong(id) else studioRepository.deleteLocalSong(id)
            if (res.isSuccess) {
                localSongManager.deleteSong(id)
                songDao.deleteSong(id)
                _uiState.update { it.copy(songs = it.songs.filter { s -> s.id != id }) }
                AppToastManager.showMessage("Видалено")
            } else AppToastManager.showMessage(res.exceptionOrNull()?.message ?: "Помилка")
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val temp = File.createTempFile("upload", null, context.cacheDir)
            temp.outputStream().use { out -> input.use { it.copyTo(out) } }
            temp
        } catch (e: Exception) { e.printStackTrace(); null }
    }
}
