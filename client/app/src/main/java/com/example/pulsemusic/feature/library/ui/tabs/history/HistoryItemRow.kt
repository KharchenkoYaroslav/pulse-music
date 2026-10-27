package com.example.pulsemusic.feature.library.ui.tabs.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pulsemusic.feature.library.data.model.SongHistoryItem
import com.example.pulsemusic.core.ui.components.MenuItemAction
import com.example.pulsemusic.core.ui.components.MoreOptionsMenu

// Список елементів(пісень) в історії
@Composable
fun HistoryItemRow(
    item: SongHistoryItem, 
    formatTimestamp: (Long) -> String,
    onClick: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAuthorClick: ((String, String?) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.cover,
            contentDescription = item.title,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title, 
                color = Color.White, 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val dateStr = remember(item.viewedAt) { formatTimestamp(item.viewedAt) }
            Text(
                text = dateStr,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1
            )
            val authorText = if (!item.originalAuthor.isNullOrBlank()) {
                "${item.authorLogin} (Ориг: ${item.originalAuthor})"
            } else {
                item.authorLogin
            }
            
            val authorModifier = if (onAuthorClick != null) {
                Modifier.clickable { onAuthorClick(item.authorId, item.authorLogin) }
            } else {
                Modifier
            }
            
            Text(
                text = authorText,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = authorModifier
            )
        }
        
        MoreOptionsMenu(
            contentDescription = "Опції пісні",
            items = listOf(
                MenuItemAction("Відтворити наступним", onPlayNext),
                MenuItemAction("Додати до черги", onAddToQueue)
            )
        )
    }
}
