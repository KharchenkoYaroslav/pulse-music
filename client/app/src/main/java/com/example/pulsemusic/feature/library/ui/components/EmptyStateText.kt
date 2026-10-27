package com.example.pulsemusic.feature.library.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Повідомлення про відсутність елементів
@Composable
fun EmptyStateText(text: String) {
    Text(
        text = text,
        color = Color.Gray,
        modifier = Modifier
            .padding(top = 32.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}
