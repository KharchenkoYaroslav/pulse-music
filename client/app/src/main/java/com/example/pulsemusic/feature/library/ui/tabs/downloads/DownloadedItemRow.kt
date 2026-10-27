package com.example.pulsemusic.feature.library.ui.tabs.downloads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.pulsemusic.feature.player.data.model.Song
import com.example.pulsemusic.core.ui.components.BaseItemRow
import com.example.pulsemusic.core.ui.components.MenuItemAction

// Список елементів(пісень) в завантажуваних
@Composable
fun DownloadedItemRow(
    item: Song,
    onClick: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onDelete: () -> Unit,
    onAuthorClick: ((String, String?) -> Unit)? = null
) {
    val menuItems = remember(onDelete, onPlayNext, onAddToQueue) {
        listOf(
            MenuItemAction("Відтворити наступним", onPlayNext),
            MenuItemAction("Додати до черги", onAddToQueue),
            MenuItemAction("Видалити", onDelete, color = Color.Red)
        )
    }

    val authorText = if (!item.originalAuthor.isNullOrBlank()) {
        "${item.authorLogin} (Ориг: ${item.originalAuthor})"
    } else {
        item.authorLogin ?: "Невідомий виконавець"
    }

    BaseItemRow(
        title = item.title,
        subtitle = authorText,
        coverUrl = item.cover ?: "",
        onClick = onClick,
        menuItems = menuItems,
        onSubtitleClick = { onAuthorClick?.invoke(item.authorId, item.authorLogin) },
        contentDescription = "Опції завантаженої пісні"
    )
}
