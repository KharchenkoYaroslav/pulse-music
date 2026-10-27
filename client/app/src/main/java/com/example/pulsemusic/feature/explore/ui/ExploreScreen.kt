package com.example.pulsemusic.feature.explore.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pulsemusic.feature.player.ui.PlayerViewModel
import com.example.pulsemusic.feature.player.data.model.PlayerSource
import com.example.pulsemusic.core.ui.components.MainTopBar
import com.example.pulsemusic.core.ui.components.FilterChipRow
import com.example.pulsemusic.core.ui.components.BaseItemRow
import com.example.pulsemusic.core.ui.components.MenuItemAction
import com.example.pulsemusic.core.ui.theme.YtDarkBackground
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel

// Головний екран
@Composable
fun ExploreScreen(
    playerViewModel: PlayerViewModel,
    exploreViewModel: ExploreViewModel = hiltViewModel()
) {
    // Спостереження за станом UI з ViewModel та ініціалізація стану списку/корутин
    val uiState by exploreViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Обчислення поточної сторінки перегляду на основі позиції прокручування списку
    val currentViewingPage by remember(uiState.loadedStartPage, uiState.pageSize, uiState.totalPages) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportStart = layoutInfo.viewportStartOffset + layoutInfo.beforeContentPadding
            val viewportEnd = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding

            val lastFullyVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull { item ->
                item.offset >= viewportStart && (item.offset + item.size) <= viewportEnd
            }

            val targetIndex = lastFullyVisibleItem?.index ?: listState.firstVisibleItemIndex
            val pageOffset = if (uiState.pageSize > 0) targetIndex / uiState.pageSize else 0
            (uiState.loadedStartPage + pageOffset).coerceIn(1, uiState.totalPages)
        }
    }

    // Ефект для нескінченної прокрутки (автоматичне завантаження попередньої або наступної сторінки)
    LaunchedEffect(listState, uiState.songs.size, uiState.loadedStartPage, uiState.loadedEndPage) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            Pair(firstVisible, lastVisible)
        }.collect { (firstIndex, lastIndex) ->
            // Якщо користувач наблизився до початку списку і є попередні сторінки
            if (firstIndex != null && firstIndex <= 1 && uiState.loadedStartPage > 1 && !uiState.isLoading) {
                exploreViewModel.loadPreviousPage()
            }
            // Якщо користувач наблизився до кінця списку і є наступні сторінки
            if (lastIndex != null && lastIndex >= uiState.songs.size - 2 && uiState.loadedEndPage < uiState.totalPages && !uiState.isLoading) {
                exploreViewModel.loadNextPage()
            }
        }
    }

    // Головний контейнер екрана
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YtDarkBackground)
    ) {
        // Верхня панель пошуку та навігації
        MainTopBar(
            onSearch = { query ->
                exploreViewModel.onSearchQueryChanged(query)
                coroutineScope.launch {
                    listState.scrollToItem(0)
                }
            },
            // Заголовок з автором, якщо вибрано конкретного автора
            customTitleContent = if (uiState.selectedAuthorId != null) {
                {
                    val authorLogin = uiState.selectedAuthorName
                        ?: uiState.songs.firstOrNull { it.authorId == uiState.selectedAuthorId }?.authorLogin
                        ?: uiState.selectedAuthorId!!
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { exploreViewModel.onAuthorFilterChanged(null) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = authorLogin,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else null
        )

        // Стан активного меню фільтрів ("SORTING" або "CATEGORIES")
        var activeFilterMenu by remember { mutableStateOf<String?>(null) }

        // Скидання меню категорій, якщо вибрано автора
        LaunchedEffect(uiState.selectedAuthorId) {
            if (uiState.selectedAuthorId != null && activeFilterMenu == "CATEGORIES") {
                activeFilterMenu = null
            }
        }

        // Верхній панельний рядок: кнопки сортування, категорій та лічильник сторінок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка перемикання меню сортування
            TextButton(
                onClick = { 
                    activeFilterMenu = if (activeFilterMenu == "SORTING") null else "SORTING"
                }
            ) {
                Text(
                    text = "Сортування",
                    color = if (activeFilterMenu == "SORTING") MaterialTheme.colorScheme.primary else Color.White
                )
            }
            
            // Кнопка перемикання меню категорій (доступна, якщо не вибрано автора)
            if (uiState.selectedAuthorId == null) {
                TextButton(
                    onClick = { 
                        activeFilterMenu = if (activeFilterMenu == "CATEGORIES") null else "CATEGORIES"
                    }
                ) {
                    Text(
                        text = "Категорії",
                        color = if (activeFilterMenu == "CATEGORIES" || uiState.selectedCategoryId != null) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Кнопка відкриття діалогу переходу на конкретну сторінку
            var showPageDialog by remember { mutableStateOf(false) }
            TextButton(onClick = { showPageDialog = true }) {
                Text("$currentViewingPage / ${uiState.totalPages}", color = Color.White)
            }
            
            // Діалог введення номера сторінки для швидкого переходу
            if (showPageDialog) {
                var pageInput by remember { mutableStateOf(currentViewingPage.toString()) }
                AlertDialog(
                    onDismissRequest = { showPageDialog = false },
                    title = { Text("Перейти на сторінку") },
                    text = {
                        OutlinedTextField(
                            value = pageInput,
                            onValueChange = { pageInput = it.filter { char -> char.isDigit() } },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            pageInput.toIntOrNull()?.let { page ->
                                exploreViewModel.jumpToPage(page)
                                coroutineScope.launch {
                                    listState.scrollToItem(0)
                                }
                            }
                            showPageDialog = false
                        }) { Text("Перейти") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPageDialog = false }) { Text("Скасувати") }
                    }
                )
            }
        }

        // Анімований блок фільтрів сортування
        AnimatedVisibility(visible = activeFilterMenu == "SORTING") {
            FilterChipRow(
                items = ExploreSortOption.entries,
                labelProvider = { it.title },
                isSelected = { uiState.sortOption == it },
                onItemClick = { exploreViewModel.onSortOptionChanged(it) },
                keyProvider = { it.name },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Анімований блок категорій
        AnimatedVisibility(visible = activeFilterMenu == "CATEGORIES" && uiState.selectedAuthorId == null) {
            FilterChipRow(
                items = uiState.categories,
                labelProvider = { it.title },
                isSelected = { uiState.selectedCategoryId == it.id },
                onItemClick = { exploreViewModel.onCategoryChanged(it.id) },
                keyProvider = { it.id },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Стрічка пісень зі спостереженням за висотою контейнера
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            LaunchedEffect(maxHeight) {
                exploreViewModel.onContainerHeightChanged(maxHeight.value)
            }

            // Елементи пісень
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.songs, key = { it.id }) { song ->
                    val authorText = if (!song.originalAuthor.isNullOrBlank()) {
                        "${song.authorLogin} (Ориг: ${song.originalAuthor})"
                    } else {
                        song.authorLogin
                    }

                    val menuItems = remember(song.id) {
                        listOf(
                            MenuItemAction(
                                text = "Відтворити наступним",
                                onClick = { playerViewModel.playNextInQueue(song.id, PlayerSource.EXTERNAL) }
                            ),
                            MenuItemAction(
                                text = "Додати до черги",
                                onClick = { playerViewModel.addToQueueEnd(song.id, PlayerSource.EXTERNAL) }
                            )
                        )
                    }

                    BaseItemRow(
                        title = song.title,
                        subtitle = authorText,
                        coverUrl = song.cover ?: "",
                        onClick = {
                            // Відтворення пісні
                            val startIndex = uiState.songs.indexOf(song)
                            playerViewModel.setQueue(
                                songIds = uiState.songs.map { it.id },
                                startIndex = if (startIndex >= 0) startIndex else 0,
                                source = PlayerSource.EXTERNAL
                            )
                        },
                        menuItems = menuItems,
                        onSubtitleClick = {
                            exploreViewModel.onAuthorFilterChanged(song.authorId, song.authorLogin)
                        }
                    )
                }

                // Індикатор завантаження внизу списку під час підвантаження даних
                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

