package com.example.pulsemusic.feature.library.ui.tabs.mysongs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.ui.components.BaseItemRow
import com.example.pulsemusic.core.ui.components.MenuItemAction

// Список елементів(пісень) в моїх піснях
@Composable
fun MySongItemRow(
    item: SongRankingItem,
    onClick: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAuthorClick: ((String, String?) -> Unit)? = null
) {
    val menuItems = remember(onPlayNext, onAddToQueue, onEdit, onDelete) {
        listOf(
            MenuItemAction("Відтворити наступним", onPlayNext),
            MenuItemAction("Додати до черги", onAddToQueue),
            MenuItemAction("Редагувати", onEdit),
            MenuItemAction("Видалити", onDelete)
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
        onSubtitleClick = { onAuthorClick?.invoke(item.authorId, item.authorLogin) }
    )
}
