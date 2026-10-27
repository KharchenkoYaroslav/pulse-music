package com.example.pulsemusic.feature.library.ui.tabs.playlists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.ui.components.BaseItemRow
import com.example.pulsemusic.core.ui.components.MenuItemAction

// Список елементів(пісень) в плейлисті
@Composable
fun PlaylistItemRow(
    item: SongRankingItem,
    onClick: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onRemoveFromPlaylist: () -> Unit,
    onAuthorClick: ((String, String?) -> Unit)? = null
) {
    val menuItems = remember(onRemoveFromPlaylist, onPlayNext, onAddToQueue) {
        listOf(
            MenuItemAction("Відтворити наступним", onPlayNext),
            MenuItemAction("Додати до черги", onAddToQueue),
            MenuItemAction("Видалити з плейлисту", onRemoveFromPlaylist, color = Color.Red)
        )
    }

    val authorText = if (!item.originalAuthor.isNullOrBlank()) {
        "${item.authorLogin} (Ориг: ${item.originalAuthor})"
    } else {
        item.authorLogin
    }

    BaseItemRow(
        title = item.title,
        subtitle = authorText,
        coverUrl = item.cover ?: "",
        onClick = onClick,
        menuItems = menuItems,
        onSubtitleClick = { onAuthorClick?.invoke(item.authorId, item.authorLogin) },
        contentDescription = "Опції пісні плейлисту"
    )
}
