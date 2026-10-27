package com.example.pulsemusic.feature.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pulsemusic.feature.player.data.model.Song
import com.example.pulsemusic.core.ui.theme.YtRedAccent

// Лист черги пісень
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    queueSongs: List<Song>,
    currentQueueIndex: Int,
    onDismissRequest: () -> Unit,
    onPlayQueueSongAt: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF141414)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Черга прослуховування",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (queueSongs.isNotEmpty()) {
                    Text(
                        text = "${queueSongs.size} треків",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            if (queueSongs.isEmpty()) {
                Text(
                    text = "Черга порожня",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(queueSongs) { index, queueSong ->
                        val isCurrent = index == currentQueueIndex
                        val itemColor = if (isCurrent) YtRedAccent else Color.White

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = queueSong.title,
                                    color = itemColor,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            supportingContent = {
                                val authorText = queueSong.originalAuthor?.takeIf { it.isNotBlank() }
                                    ?: queueSong.authorLogin?.takeIf { it.isNotBlank() }
                                    ?: queueSong.authorId
                                if (authorText.isNotBlank()) {
                                    Text(
                                        text = authorText,
                                        color = if (isCurrent) itemColor.copy(alpha = 0.8f) else Color.Gray,
                                        maxLines = 1
                                    )
                                }
                            },
                            leadingContent = {
                                Box(contentAlignment = Alignment.Center) {
                                    AsyncImage(
                                        model = queueSong.cover,
                                        contentDescription = queueSong.title,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (isCurrent) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.4f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Playing",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = { onRemoveFromQueue(index) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove from queue",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (isCurrent) Color(0xFF222222) else Color.Transparent
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPlayQueueSongAt(index) }
                        )
                    }
                }
            }
        }
    }
}
