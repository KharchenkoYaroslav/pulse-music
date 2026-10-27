package com.example.pulsemusic.feature.library.ui.tabs.likes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.pulsemusic.feature.library.ui.components.EmptyStateText
import com.example.pulsemusic.feature.player.data.model.PlayerSource
import com.example.pulsemusic.feature.player.ui.PlayerViewModel

// Вкладка вподобань
@Composable
fun LikesTabContent(
    viewModel: LikesViewModel,
    searchQuery: String,
    pageSize: Int,
    playerViewModel: PlayerViewModel,
    onAuthorClick: ((String, String?) -> Unit)?
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

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
        if (uiState.likes.isEmpty() && !uiState.isLoading) {
            item { EmptyStateText(if (searchQuery.isNotEmpty()) "Нічого не знайдено" else "Немає вподобаних пісень") }
        } else {
            itemsIndexed(
                items = uiState.likes,
                key = { index, item -> "${item.id}_$index" }
            ) { _, item ->
                LikeItemRow(
                    item = item,
                    onClick = { playerViewModel.playSingleSong(item.id, PlayerSource.EXTERNAL) },
                    onPlayNext = { playerViewModel.playNextInQueue(item.id, PlayerSource.EXTERNAL) },
                    onAddToQueue = { playerViewModel.addToQueueEnd(item.id, PlayerSource.EXTERNAL) },
                    onToggleLike = { viewModel.toggleLike(item.id) },
                    onAuthorClick = onAuthorClick
                )
            }
        }
    }
}
