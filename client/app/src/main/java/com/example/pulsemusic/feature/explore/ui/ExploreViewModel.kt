package com.example.pulsemusic.feature.explore.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsemusic.core.data.CoreRepository
import com.example.pulsemusic.core.data.model.CategoryListResponse
import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.feature.explore.data.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

enum class ExploreSortOption(val title: String) {
    NEWEST("Найновіші"),
    BY_LIKES("Найбільш вподобані"),
    BY_VIEWS("Найпопулярніші")
}

enum class LoadMode {
    REPLACE,
    PREPEND,
    APPEND
}

data class ExploreUiState(
    val songs: List<SongRankingItem> = emptyList(),
    val sortOption: ExploreSortOption = ExploreSortOption.NEWEST,
    val selectedCategoryId: String? = null,
    val selectedAuthorId: String? = null,
    val selectedAuthorName: String? = null,
    val searchQuery: String? = null,
    val loadedStartPage: Int = 1,
    val loadedEndPage: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val pageSize: Int = 10,
    val itemHeightDp: Float = 72f, // 56.dp cover + 16.dp vertical padding
    val isLoading: Boolean = false,
    val categories: List<CategoryListResponse> = emptyList(),
    val error: String? = null
)

// Модель представлення Головного екрану
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val coreRepository: CoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        viewModelScope.launch {
            coreRepository.refreshCategories()
        }
        viewModelScope.launch {
            coreRepository.observeCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
        
        loadInitialData()
    }

    // ## Завантаження та пагінація даних

    // Первинне завантаження даних (перша сторінка)
    fun loadInitialData() {
        val currentState = _uiState.value
        loadPage(
            page = 1,
            mode = LoadMode.REPLACE,
            sortOption = currentState.sortOption,
            categoryId = currentState.selectedCategoryId,
            authorId = currentState.selectedAuthorId,
            searchQuery = currentState.searchQuery
        )
    }

    // Перехід на конкретну сторінку
    fun jumpToPage(page: Int) {
        val currentState = _uiState.value
        loadPage(
            page = page,
            mode = LoadMode.REPLACE,
            sortOption = currentState.sortOption,
            categoryId = currentState.selectedCategoryId,
            authorId = currentState.selectedAuthorId,
            searchQuery = currentState.searchQuery
        )
    }

    // Завантаження наступної сторінки
    fun loadNextPage() {
        val state = _uiState.value
        if (state.loadedEndPage < state.totalPages && !state.isLoading) {
            loadPage(
                page = state.loadedEndPage + 1,
                mode = LoadMode.APPEND,
                sortOption = state.sortOption,
                categoryId = state.selectedCategoryId,
                authorId = state.selectedAuthorId,
                searchQuery = state.searchQuery
            )
        }
    }

    // Завантаження попередньої сторінки
    fun loadPreviousPage() {
        val state = _uiState.value
        if (state.loadedStartPage > 1 && !state.isLoading) {
            loadPage(
                page = state.loadedStartPage - 1,
                mode = LoadMode.PREPEND,
                sortOption = state.sortOption,
                categoryId = state.selectedCategoryId,
                authorId = state.selectedAuthorId,
                searchQuery = state.searchQuery
            )
        }
    }

    // Завантаження сторінки з репозиторію згідно з обраним режимом
    private fun loadPage(
        page: Int,
        mode: LoadMode,
        sortOption: ExploreSortOption = _uiState.value.sortOption,
        categoryId: String? = _uiState.value.selectedCategoryId,
        authorId: String? = _uiState.value.selectedAuthorId,
        searchQuery: String? = _uiState.value.searchQuery,
        pageSize: Int = _uiState.value.pageSize
    ) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    error = null,
                    sortOption = sortOption,
                    selectedCategoryId = categoryId,
                    selectedAuthorId = authorId,
                    searchQuery = searchQuery
                ) 
            }
            
            var currentTotalCount = 0
            if (mode == LoadMode.REPLACE && page == 1) {
                val countResult = when (sortOption) {
                    ExploreSortOption.NEWEST -> exploreRepository.getCountByNewest(categoryId, authorId, searchQuery)
                    ExploreSortOption.BY_LIKES -> exploreRepository.getCountByLikes(categoryId, authorId, searchQuery)
                    ExploreSortOption.BY_VIEWS -> exploreRepository.getCountByViews(categoryId, authorId, searchQuery)
                }
                
                if (countResult.isSuccess) {
                    currentTotalCount = countResult.getOrDefault(0)
                }
            } else {
                currentTotalCount = _uiState.value.totalCount
            }

            val totalPages = if (currentTotalCount == 0) 1 else ceil(currentTotalCount.toDouble() / pageSize).toInt()
            val safePage = page.coerceIn(1, totalPages)
            
            val from = (safePage - 1) * pageSize
            val to = from + pageSize

            val itemsResult = when (sortOption) {
                ExploreSortOption.NEWEST -> exploreRepository.getTrendingByNewestDirectly(from, to, categoryId, authorId, searchQuery)
                ExploreSortOption.BY_LIKES -> exploreRepository.getTrendingByLikesDirectly(from, to, categoryId, authorId, searchQuery)
                ExploreSortOption.BY_VIEWS -> exploreRepository.getTrendingByViewsDirectly(from, to, categoryId, authorId, searchQuery)
            }

            if (itemsResult.isSuccess) {
                val newItems = itemsResult.getOrDefault(emptyList())
                _uiState.update { state ->
                    val (updatedSongs, newStartPage, newEndPage) = when (mode) {
                        LoadMode.REPLACE -> Triple(newItems, safePage, safePage)
                        LoadMode.APPEND -> {
                            val combined = (state.songs + newItems).distinctBy { it.id }
                            Triple(combined, state.loadedStartPage, safePage)
                        }
                        LoadMode.PREPEND -> {
                            val combined = (newItems + state.songs).distinctBy { it.id }
                            Triple(combined, safePage, state.loadedEndPage)
                        }
                    }

                    state.copy(
                        songs = updatedSongs,
                        totalCount = currentTotalCount,
                        totalPages = totalPages,
                        loadedStartPage = newStartPage,
                        loadedEndPage = newEndPage,
                        pageSize = pageSize,
                        sortOption = sortOption,
                        selectedCategoryId = categoryId,
                        selectedAuthorId = authorId,
                        searchQuery = searchQuery,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = itemsResult.exceptionOrNull()?.message ?: "Unknown error"
                    ) 
                }
            }
        }
    }

    // ## Фільтрація, сортування та пошук

    // Зміна тексту в полі пошуку
    fun onSearchQueryChanged(query: String) {
        val formattedQuery = query.trim().ifEmpty { null }
        if (_uiState.value.searchQuery != formattedQuery) {
            _uiState.update { it.copy(searchQuery = formattedQuery) }
            loadPage(
                page = 1,
                mode = LoadMode.REPLACE,
                sortOption = _uiState.value.sortOption,
                categoryId = _uiState.value.selectedCategoryId,
                authorId = _uiState.value.selectedAuthorId,
                searchQuery = formattedQuery
            )
        }
    }

    // Зміна параметра сортування (найновіші, за лайками, за переглядами)
    fun onSortOptionChanged(option: ExploreSortOption) {
        if (_uiState.value.sortOption != option) {
            _uiState.update { it.copy(sortOption = option) }
            loadPage(
                page = 1,
                mode = LoadMode.REPLACE,
                sortOption = option,
                categoryId = _uiState.value.selectedCategoryId,
                authorId = _uiState.value.selectedAuthorId,
                searchQuery = _uiState.value.searchQuery
            )
        }
    }

    // Вибір або скасування вибору категорії
    fun onCategoryChanged(categoryId: String?) {
        val newCategory = if (_uiState.value.selectedCategoryId == categoryId) null else categoryId
        _uiState.update { it.copy(selectedCategoryId = newCategory) }
        loadPage(
            page = 1,
            mode = LoadMode.REPLACE,
            sortOption = _uiState.value.sortOption,
            categoryId = newCategory,
            authorId = _uiState.value.selectedAuthorId,
            searchQuery = _uiState.value.searchQuery
        )
    }

    // Встановлення або скидання фільтра за автором
    fun onAuthorFilterChanged(authorId: String?, authorName: String? = null) {
        _uiState.update {
            it.copy(
                selectedAuthorId = authorId,
                selectedAuthorName = if (authorId != null) authorName else null,
                selectedCategoryId = null,
                searchQuery = null,
                isLoading = authorId != null,
                songs = if (authorId != _uiState.value.selectedAuthorId) emptyList() else it.songs
            )
        }
        loadPage(
            page = 1,
            mode = LoadMode.REPLACE,
            sortOption = _uiState.value.sortOption,
            categoryId = null,
            authorId = authorId,
            searchQuery = null
        )
    }

    // ## Розмір контейнера та розмір сторінки

    // Автоматичне обчислення кількості елементів на сторінці при зміні висоти контейнера
    fun onContainerHeightChanged(heightDp: Float) {
        val itemHeightDp = _uiState.value.itemHeightDp
        val calculatedPageSize = (heightDp / itemHeightDp).toInt().coerceAtLeast(1)
        updatePageSize(calculatedPageSize)
    }

    // Оновлення розміру сторінки та перезавантаження даних
    private fun updatePageSize(newPageSize: Int) {
        if (newPageSize > 0 && newPageSize != _uiState.value.pageSize) {
            val currentState = _uiState.value
            _uiState.update { it.copy(pageSize = newPageSize) }
            loadPage(
                page = 1,
                mode = LoadMode.REPLACE,
                sortOption = currentState.sortOption,
                categoryId = currentState.selectedCategoryId,
                authorId = currentState.selectedAuthorId,
                searchQuery = currentState.searchQuery,
                pageSize = newPageSize
            )
        }
    }
}
