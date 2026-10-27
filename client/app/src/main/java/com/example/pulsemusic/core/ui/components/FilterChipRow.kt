package com.example.pulsemusic.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Компонент стрічки фільтрів
@Composable
fun <T> FilterChipRow(
    items: List<T>,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier,
    isSelected: (T) -> Boolean = { true },
    onItemClick: ((T) -> Unit)? = null,
    keyProvider: ((T) -> Any)? = null
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(
            items = items,
            key = keyProvider
        ) { item ->
            FilterChip(
                selected = isSelected(item),
                onClick = { onItemClick?.invoke(item) },
                label = { Text(labelProvider(item)) }
            )
        }
    }
}
