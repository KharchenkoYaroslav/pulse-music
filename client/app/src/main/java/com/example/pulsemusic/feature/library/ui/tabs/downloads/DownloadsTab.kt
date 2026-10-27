package com.example.pulsemusic.feature.library.ui.tabs.downloads

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.pulsemusic.core.ui.components.AppDialog
import com.example.pulsemusic.core.ui.theme.YtRedAccent
import com.example.pulsemusic.feature.library.ui.components.EmptyStateText
import com.example.pulsemusic.feature.player.data.model.PlayerSource
import com.example.pulsemusic.feature.player.data.model.Song
import com.example.pulsemusic.feature.player.ui.PlayerViewModel

// Вкладка завантажуваних
@Composable
fun DownloadsTabContent(
    viewModel: DownloadsViewModel,
    searchQuery: String,
    playerViewModel: PlayerViewModel,
    onAuthorClick: ((String, String?) -> Unit)?
) {
    val songs by viewModel.downloadedSongs.collectAsState()
    var songToDelete by remember { mutableStateOf<Song?>(null) }

    val filtered = remember(songs, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) songs else songs.filter {
            it.title.lowercase().contains(q) || it.authorLogin?.lowercase()?.contains(q) == true
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (filtered.isEmpty()) {
            item { EmptyStateText(if (searchQuery.isNotEmpty()) "Нічого не знайдено" else "Немає завантажених пісень") }
        } else {
            itemsIndexed(items = filtered, key = { _, item -> item.id }) { _, item ->
                DownloadedItemRow(
                    item = item,
                    onClick = { playerViewModel.playSingleSong(item.id, PlayerSource.LOCAL) },
                    onPlayNext = { playerViewModel.playNextInQueue(item.id, PlayerSource.LOCAL) },
                    onAddToQueue = { playerViewModel.addToQueueEnd(item.id, PlayerSource.LOCAL) },
                    onDelete = { songToDelete = item },
                    onAuthorClick = onAuthorClick
                )
            }
        }
    }

    if (songToDelete != null) {
        AppDialog(
            onDismissRequest = { songToDelete = null },
            title = "Видалити з пристрою?",
            confirmButtonText = "Видалити",
            confirmButtonColor = YtRedAccent,
            onConfirm = {
                viewModel.deleteSong(songToDelete!!.id)
                songToDelete = null
            }
        ) {
            Text("Ви дійсно хочете видалити ${songToDelete?.title}?", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
