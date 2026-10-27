package com.example.pulsemusic.feature.library.ui.tabs.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.core.util.groupItemsByTimeCategory
import com.example.pulsemusic.feature.library.data.LibraryRepository
import com.example.pulsemusic.feature.library.data.model.SongHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val history: List<SongHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
) {
    val groupedHistory: Map<String, List<SongHistoryItem>>
        get() = groupItemsByTimeCategory(history) { it.viewedAt }
}

// Модель представлення історії
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var hasMore = true
    private var offset = 0
    private var loadJob: Job? = null

    fun loadInitial(searchQuery: String, pageSize: Int) {
        if (authPreferences.accessToken.value.isNullOrBlank()) return
        loadJob?.cancel()
        hasMore = true
        offset = 0
        _uiState.update { it.copy(isLoading = true) }

        loadJob = viewModelScope.launch {
            val query = searchQuery.trim().takeIf { it.isNotEmpty() }
            val result = repository.getHistoryDirectly(from = 0, to = pageSize, search = query)
            val items = result.getOrDefault(emptyList())
            hasMore = items.size >= pageSize
            offset = pageSize
            _uiState.update { it.copy(history = items, isLoading = false, isError = !result.isSuccess) }
        }
    }

    fun loadMore(searchQuery: String, pageSize: Int) {
        if (_uiState.value.isLoading || !hasMore || authPreferences.accessToken.value.isNullOrBlank()) return
        _uiState.update { it.copy(isLoading = true) }

        loadJob = viewModelScope.launch {
            val query = searchQuery.trim().takeIf { it.isNotEmpty() }
            val result = repository.getHistoryDirectly(from = offset, to = offset + pageSize, search = query)
            val newItems = result.getOrDefault(emptyList())

            hasMore = newItems.size >= pageSize
            val current = _uiState.value.history
            val uniqueNew = newItems.filter { n -> current.none { it.id == n.id && it.viewedAt == n.viewedAt } }

            _uiState.update { it.copy(history = current + uniqueNew, isLoading = false) }
            offset += pageSize
        }
    }
}
