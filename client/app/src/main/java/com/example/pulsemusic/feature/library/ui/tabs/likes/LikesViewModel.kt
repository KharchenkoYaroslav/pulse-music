package com.example.pulsemusic.feature.library.ui.tabs.likes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.core.data.CoreRepository
import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.feature.library.data.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LikesUiState(
    val likes: List<SongRankingItem> = emptyList(),
    val isLoading: Boolean = false
)

// Модель представлення вподобань
@HiltViewModel
class LikesViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val coreRepository: CoreRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LikesUiState())
    val uiState: StateFlow<LikesUiState> = _uiState.asStateFlow()

    private var hasMore = true
    private var offset = 0
    private var loadJob: Job? = null

    fun toggleLike(songId: String) {
        viewModelScope.launch { coreRepository.toggleLike(songId) }
    }

    fun loadInitial(searchQuery: String, pageSize: Int) {
        if (authPreferences.accessToken.value.isNullOrBlank()) return
        loadJob?.cancel()
        hasMore = true
        offset = 0
        _uiState.update { it.copy(isLoading = true) }

        loadJob = viewModelScope.launch {
            val query = searchQuery.trim().takeIf { it.isNotEmpty() }
            val result = repository.getLikesDirectly(from = 0, to = pageSize, search = query)
            val items = result.getOrDefault(emptyList())
            hasMore = items.size >= pageSize
            offset = pageSize
            _uiState.update { it.copy(likes = items, isLoading = false) }
        }
    }

    fun loadMore(searchQuery: String, pageSize: Int) {
        if (_uiState.value.isLoading || !hasMore || authPreferences.accessToken.value.isNullOrBlank()) return
        _uiState.update { it.copy(isLoading = true) }

        loadJob = viewModelScope.launch {
            val query = searchQuery.trim().takeIf { it.isNotEmpty() }
            val result = repository.getLikesDirectly(from = offset, to = offset + pageSize, search = query)
            val newItems = result.getOrDefault(emptyList())

            hasMore = newItems.size >= pageSize
            val current = _uiState.value.likes
            val uniqueNew = newItems.filter { n -> current.none { it.id == n.id } }

            _uiState.update { it.copy(likes = current + uniqueNew, isLoading = false) }
            offset += pageSize
        }
    }
}
