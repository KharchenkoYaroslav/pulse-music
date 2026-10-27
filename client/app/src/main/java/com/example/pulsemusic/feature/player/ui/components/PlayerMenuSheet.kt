package com.example.pulsemusic.feature.player.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pulsemusic.core.ui.theme.YtRedAccent

// Лист меню плеєра
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerMenuSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    isLocalSource: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    onOpenQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDeleteSong: () -> Unit,
    onDownloadSong: () -> Unit,
    onShowDescription: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF141414)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            ListItem(
                headlineContent = { Text("Черга відтворення", color = Color.White) },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = Color.White) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable {
                    onDismissRequest()
                    onOpenQueue()
                }
            )
            if (!isLocalSource) {
                ListItem(
                    headlineContent = { Text("Додати до плейлиста", color = Color.White) },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, tint = Color.White) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onDismissRequest()
                        onAddToPlaylist()
                    }
                )
            }
            if (isDownloaded) {
                ListItem(
                    headlineContent = { Text("Видалити з завантажень", color = Color.Red) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable(enabled = !isDownloading) {
                        onDismissRequest()
                        onDeleteSong()
                    }
                )
            } else {
                ListItem(
                    headlineContent = { Text("Завантажити пісню", color = Color.White) },
                    leadingContent = {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = YtRedAccent
                            )
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable(enabled = !isDownloading) {
                        onDismissRequest()
                        onDownloadSong()
                    }
                )
            }
            ListItem(
                headlineContent = { Text("Опис", color = Color.White) },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null, tint = Color.White) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable {
                    onDismissRequest()
                    onShowDescription()
                }
            )
        }
    }
}
