package com.example.pulsemusic.feature.player.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pulsemusic.core.data.model.CategoryListResponse
import com.example.pulsemusic.core.ui.components.FilterChipRow

// Лист опису пісні та її категорій
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDescriptionSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    description: String?,
    categories: List<CategoryListResponse> = emptyList()
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF141414)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            if (categories.isNotEmpty()) {
                Text(
                    text = "Категорії",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FilterChipRow(
                    items = categories,
                    labelProvider = { it.title },
                    keyProvider = { it.id },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Text(
                text = "Опис",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = description.takeIf { !it.isNullOrBlank() } ?: "Опис відсутній",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.LightGray
            )
        }
    }
}
