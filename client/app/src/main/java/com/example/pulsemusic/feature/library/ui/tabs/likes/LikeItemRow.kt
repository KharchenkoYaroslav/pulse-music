package com.example.pulsemusic.feature.library.ui.tabs.likes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.ui.components.BaseItemRow
import com.example.pulsemusic.core.ui.components.MenuItemAction

// Список елементів(пісень) в вподобаннях
@Composable
fun LikeItemRow(
    item: SongRankingItem, 
    onClick: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onToggleLike: (() -> Unit)? = null,
    onAuthorClick: ((String, String?) -> Unit)? = null
) {
    val menuItems = remember(onToggleLike, onPlayNext, onAddToQueue) {
        buildList {
            add(MenuItemAction("Відтворити наступним", onPlayNext))
            add(MenuItemAction("Додати до черги", onAddToQueue))
            if (onToggleLike != null) {
                add(MenuItemAction("Прибрати з вподобаних", onToggleLike))
            }
        }
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
        contentDescription = "Опції пісні"
    )
}
