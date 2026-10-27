package com.example.pulsemusic.feature.library.ui.tabs.playlists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsemusic.core.ui.theme.YtRedAccent
import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.data.model.UserPlaylist
import com.example.pulsemusic.core.ui.components.AppDialog
import com.example.pulsemusic.core.ui.components.AppTextField
import com.example.pulsemusic.core.ui.components.MenuItemAction
import com.example.pulsemusic.core.ui.components.MoreOptionsMenu

// Список плейлистів
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistRow(
    item: UserPlaylist,
    songs: List<SongRankingItem>?,
    isLoadingSongs: Boolean,
    onExpand: () -> Unit,
    onPlayAll: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onPlaySong: (String) -> Unit,
    onPlayNextSong: (String) -> Unit,
    onAddToQueueSong: (String) -> Unit,
    onRemoveSong: (String) -> Unit,
    onAuthorClick: ((String, String?) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(item.name) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var songToRemoveFromPlaylist by remember { mutableStateOf<SongRankingItem?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                    if (expanded) {
                        onExpand()
                    }
                }
                .padding(vertical = 0.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(spacing = MarqueeSpacing(16.dp))
                )
            }
            
            MoreOptionsMenu(
                iconTint = Color.White,
                contentDescription = "Опції плейлисту",
                items = listOf(
                    MenuItemAction(
                        text = "Відтворити все",
                        onClick = onPlayAll,
                        enabled = songs == null || songs.isNotEmpty()
                    ),
                    MenuItemAction(
                        text = "Перейменувати",
                        onClick = {
                            renameText = item.name
                            showRenameDialog = true
                        }
                    ),
                    MenuItemAction(
                        text = "Видалити",
                        onClick = { showDeleteDialog = true },
                        color = Color.Red
                    )
                )
            )
            
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Згорнути" else "Розгорнути",
                tint = Color.Gray
            )
        }

        if (expanded) {
            if (isLoadingSongs) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                }
            } else if (songs != null) {
                if (songs.isEmpty()) {
                    Text(
                        text = "Плейлист порожній",
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        songs.forEach { song ->
                            PlaylistItemRow(
                                item = song,
                                onClick = { onPlaySong(song.id) },
                                onPlayNext = { onPlayNextSong(song.id) },
                                onAddToQueue = { onAddToQueueSong(song.id) },
                                onRemoveFromPlaylist = { songToRemoveFromPlaylist = song },
                                onAuthorClick = onAuthorClick
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        AppDialog(
            onDismissRequest = { showRenameDialog = false },
            title = "Перейменувати плейлист",
            confirmButtonText = "Зберегти",
            onConfirm = {
                if (renameText.isNotBlank()) {
                    onRename(renameText)
                }
                showRenameDialog = false
            }
        ) {
            AppTextField(
                value = renameText,
                onValueChange = { renameText = it },
                label = "Назва"
            )
        }
    }

    if (showDeleteDialog) {
        AppDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "Видалити плейлист",
            confirmButtonText = "Так, видалити",
            confirmButtonColor = YtRedAccent,
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            }
        ) {
            Text(
                text = "Ви дійсно бажаєте видалити плейлист «${item.name}»?",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    if (songToRemoveFromPlaylist != null) {
        AppDialog(
            onDismissRequest = { songToRemoveFromPlaylist = null },
            title = "Видалити з плейлисту",
            confirmButtonText = "Так, видалити",
            confirmButtonColor = YtRedAccent,
            onConfirm = {
                val song = songToRemoveFromPlaylist
                if (song != null) {
                    onRemoveSong(song.id)
                }
                songToRemoveFromPlaylist = null
            }
        ) {
            Text(
                text = "Ви дійсно бажаєте видалити пісню «${songToRemoveFromPlaylist?.title}» з плейлисту?",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
