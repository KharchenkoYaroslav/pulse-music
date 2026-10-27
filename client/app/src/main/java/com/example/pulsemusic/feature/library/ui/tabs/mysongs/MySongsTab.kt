package com.example.pulsemusic.feature.library.ui.tabs.mysongs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.ui.components.AppDialog
import com.example.pulsemusic.core.ui.theme.YtRedAccent
import com.example.pulsemusic.feature.library.ui.components.EmptyStateText
import com.example.pulsemusic.feature.player.data.model.PlayerSource
import com.example.pulsemusic.feature.player.ui.PlayerViewModel

// Вкладка моїх пісень(реалізація студії)
@Composable
fun MySongsTabContent(
    viewModel: MySongsViewModel,
    searchQuery: String,
    pageSize: Int,
    playerViewModel: PlayerViewModel,
    onAuthorClick: ((String, String?) -> Unit)?
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showDialog by remember { mutableStateOf(false) }
    var editingSong by remember { mutableStateOf<SongRankingItem?>(null) }
    var songToDelete by remember { mutableStateOf<SongRankingItem?>(null) }

    LaunchedEffect(searchQuery, pageSize) { viewModel.loadInitial(searchQuery, pageSize) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIdx ->
                val total = listState.layoutInfo.totalItemsCount
                if (lastIdx != null && total > 0 && lastIdx >= total - 2) viewModel.loadMore(searchQuery, pageSize)
            }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        if (uiState.songs.isEmpty() && !uiState.isLoading) {
            item { EmptyStateText(if (searchQuery.isNotEmpty()) "Нічого не знайдено" else "У вас немає власних пісень") }
        } else {
            itemsIndexed(items = uiState.songs, key = { _, item -> item.id }) { _, item ->
                MySongItemRow(
                    item = item,
                    onClick = { playerViewModel.playSingleSong(item.id, PlayerSource.EXTERNAL) },
                    onPlayNext = { playerViewModel.playNextInQueue(item.id, PlayerSource.EXTERNAL) },
                    onAddToQueue = { playerViewModel.addToQueueEnd(item.id, PlayerSource.EXTERNAL) },
                    onEdit = { editingSong = item; showDialog = true },
                    onDelete = { songToDelete = item },
                    onAuthorClick = onAuthorClick
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { editingSong = null; showDialog = true }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray).padding(12.dp))
                Text("Додати нову пісню", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    if (showDialog) {
        MySongDialog(
            item = editingSong,
            categories = uiState.categories,
            onDismissRequest = { showDialog = false; editingSong = null },
            onConfirmLocal = { title, desc, orig, cats, sUri, cUri ->
                if (editingSong != null) viewModel.updateLocalSong(editingSong!!.id, title, desc, orig, cats, sUri, cUri, searchQuery, pageSize)
                else viewModel.createLocalSong(title, desc, orig, cats, sUri, cUri, searchQuery, pageSize)
                showDialog = false; editingSong = null
            },
            onConfirmExternal = { url, cats ->
                if (editingSong != null) viewModel.updateExternalSong(editingSong!!.id, url, cats, searchQuery, pageSize)
                else viewModel.createExternalSong(url, cats, searchQuery, pageSize)
                showDialog = false; editingSong = null
            }
        )
    }

    if (songToDelete != null) {
        AppDialog(
            onDismissRequest = { songToDelete = null },
            title = "Видалити пісню?",
            confirmButtonText = "Видалити",
            confirmButtonColor = YtRedAccent,
            onConfirm = { viewModel.deleteSong(songToDelete!!.id); songToDelete = null }
        ) {
            Text("Ви дійсно хочете видалити ${songToDelete?.title}?", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
