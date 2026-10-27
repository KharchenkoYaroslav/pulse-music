package com.example.pulsemusic.feature.library.ui.tabs.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.pulsemusic.core.ui.components.AppDialog
import com.example.pulsemusic.core.ui.components.AppTextField
import com.example.pulsemusic.feature.library.ui.components.EmptyStateText
import com.example.pulsemusic.feature.player.data.model.PlayerSource
import com.example.pulsemusic.feature.player.ui.PlayerViewModel

// Вкладка плейлістів
@Composable
fun PlaylistsTabContent(
    viewModel: PlaylistsViewModel,
    searchQuery: String,
    playerViewModel: PlayerViewModel,
    onAuthorClick: ((String, String?) -> Unit)?
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        viewModel.loadPlaylists(searchQuery)
    }

    val playlists = uiState.filteredPlaylists(searchQuery)
    val details = uiState.filteredDetails(searchQuery)

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (playlists.isEmpty() && !uiState.isLoading) {
            item { EmptyStateText(if (searchQuery.isNotEmpty()) "Нічого не знайдено" else "У вас немає плейлистів") }
        } else {
            itemsIndexed(items = playlists, key = { _, item -> item.id }) { _, item ->
                PlaylistRow(
                    item = item,
                    songs = details[item.id],
                    isLoadingSongs = uiState.loadingPlaylists.contains(item.id),
                    onExpand = { viewModel.loadPlaylistDetails(item.id) },
                    onPlayAll = {
                        viewModel.playAll(item.id) { songIds ->
                            playerViewModel.setQueue(songIds, 0, PlayerSource.EXTERNAL)
                        }
                    },
                    onRename = { name -> viewModel.renamePlaylist(item.id, name, searchQuery) },
                    onDelete = { viewModel.deletePlaylist(item.id) },
                    onPlaySong = { id -> playerViewModel.playSingleSong(id, PlayerSource.EXTERNAL) },
                    onPlayNextSong = { id -> playerViewModel.playNextInQueue(id, PlayerSource.EXTERNAL) },
                    onAddToQueueSong = { id -> playerViewModel.addToQueueEnd(id, PlayerSource.EXTERNAL) },
                    onRemoveSong = { songId -> viewModel.removeSong(item.id, songId) },
                    onAuthorClick = onAuthorClick
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCreateDialog = true }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray).padding(12.dp)
                )
                Text("Створити новий плейлист", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    if (showCreateDialog) {
        AppDialog(
            onDismissRequest = { showCreateDialog = false; newName = "" },
            title = "Створити плейлист",
            confirmButtonText = "Створити",
            onConfirm = {
                if (newName.isNotBlank()) viewModel.createPlaylist(newName, searchQuery)
                showCreateDialog = false
                newName = ""
            }
        ) {
            AppTextField(value = newName, onValueChange = { newName = it }, label = "Назва")
        }
    }
}
