package com.example.pulsemusic.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Базовий компонент елементу в списку пісень
@Composable
fun BaseItemRow(
    title: String,
    subtitle: String,
    coverUrl: String,
    onClick: () -> Unit,
    menuItems: List<MenuItemAction>,
    modifier: Modifier = Modifier,
    onSubtitleClick: (() -> Unit)? = null,
    imageSize: Dp = 56.dp,
    imageCornerRadius: Dp = 8.dp,
    contentDescription: String = "Опції"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = title,
            modifier = Modifier
                .size(imageSize)
                .clip(RoundedCornerShape(imageCornerRadius))
                .background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                color = Color.White, 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            val subtitleModifier = if (onSubtitleClick != null) {
                Modifier.clickable { onSubtitleClick() }
            } else {
                Modifier
            }
            
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = subtitleModifier
            )
        }
        
        if (menuItems.isNotEmpty()) {
            MoreOptionsMenu(
                contentDescription = contentDescription,
                items = menuItems
            )
        }
    }
}
