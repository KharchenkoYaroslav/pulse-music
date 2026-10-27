package com.example.pulsemusic.feature.player.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pulsemusic.feature.player.data.model.UserPlaylistState
import com.example.pulsemusic.core.ui.theme.YtRedAccent

// Лист плейлистів пісні
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSheet(
    userPlaylists: List<UserPlaylistState>,
    onDismissRequest: () -> Unit,
    onToggleSongInPlaylist: (String, (Boolean) -> Unit) -> Unit
) {
    val context = LocalContext.current
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
            Text(
                text = "Додати до плейлиста",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (userPlaylists.isEmpty()) {
                Text(
                    text = "У вас немає доступних плейлистів",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn {
                    items(userPlaylists) { item ->
                        val onToggle = {
                            val isAdding = !item.isInPlaylist
                            onToggleSongInPlaylist(item.playlist.id) { success ->
                                if (success) {
                                    val status = if (isAdding) "Додано до плейлиста" else "Видалено з плейлиста"
                                    Toast.makeText(context, "$status «${item.playlist.name}»", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = item.playlist.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            trailingContent = {
                                Checkbox(
                                    checked = item.isInPlaylist,
                                    onCheckedChange = { onToggle() },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = YtRedAccent,
                                        uncheckedColor = Color.Gray,
                                        checkmarkColor = Color.White
                                    )
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { onToggle() }
                        )
                    }
                }
            }
        }
    }
}
