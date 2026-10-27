package com.example.pulsemusic.feature.library.ui.tabs.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsemusic.core.util.rememberTimestampFormatter
import com.example.pulsemusic.feature.library.ui.components.EmptyStateText
import com.example.pulsemusic.feature.player.data.model.PlayerSource
import com.example.pulsemusic.feature.player.ui.PlayerViewModel

// Вкладка історії
@Composable
fun HistoryTabContent(
    viewModel: HistoryViewModel,
    searchQuery: String,
    pageSize: Int,
    playerViewModel: PlayerViewModel,
    onAuthorClick: ((String, String?) -> Unit)?
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val formatTimestamp = rememberTimestampFormatter()

    LaunchedEffect(searchQuery, pageSize) {
        viewModel.loadInitial(searchQuery, pageSize)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                val total = listState.layoutInfo.totalItemsCount
                if (lastIndex != null && total > 0 && lastIndex >= total - 2) {
                    viewModel.loadMore(searchQuery, pageSize)
                }
            }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        if (uiState.history.isEmpty() && !uiState.isLoading) {
            item { EmptyStateText(if (searchQuery.isNotEmpty()) "Нічого не знайдено" else "Історія порожня") }
        } else {
            uiState.groupedHistory.forEach { (category, categoryItems) ->
                item(key = category) {
                    Text(
                        text = category,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                itemsIndexed(
                    items = categoryItems,
                    key = { index, item -> "${category}_${item.id}_${item.viewedAt}_$index" }
                ) { _, item ->
                    HistoryItemRow(
                        item = item,
                        formatTimestamp = formatTimestamp,
                        onClick = { playerViewModel.playSingleSong(item.id, PlayerSource.EXTERNAL) },
                        onPlayNext = { playerViewModel.playNextInQueue(item.id, PlayerSource.EXTERNAL) },
                        onAddToQueue = { playerViewModel.addToQueueEnd(item.id, PlayerSource.EXTERNAL) },
                        onAuthorClick = onAuthorClick
                    )
                }
            }
        }
    }
}
