package com.example.pulsemusic.feature.library.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsemusic.core.data.AuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LibrarySharedState(
    val selectedTabIndex: Int = 0,
    val searchQuery: String = "",
    val pageSize: Int = 10,
    val itemHeightDp: Float = 72f
)

// Центральна модель представлення бібліотеки

@HiltViewModel
class LibraryViewModel @Inject constructor(
    authPreferences: AuthPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibrarySharedState())
    val uiState: StateFlow<LibrarySharedState> = _uiState.asStateFlow()

    val accessToken: StateFlow<String?> = authPreferences.accessToken
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setTab(index: Int) {
        if (_uiState.value.selectedTabIndex != index) {
            _uiState.update { it.copy(selectedTabIndex = index, searchQuery = "") }
        }
    }

    fun setSearchQuery(query: String) {
        if (_uiState.value.searchQuery != query) {
            _uiState.update { it.copy(searchQuery = query) }
        }
    }

    fun onContainerHeightChanged(heightDp: Float) {
        val itemHeightDp = _uiState.value.itemHeightDp
        val calculatedPageSize = (heightDp / itemHeightDp).toInt().coerceAtLeast(1)
        if (calculatedPageSize != _uiState.value.pageSize) {
            _uiState.update { it.copy(pageSize = calculatedPageSize) }
        }
    }
}