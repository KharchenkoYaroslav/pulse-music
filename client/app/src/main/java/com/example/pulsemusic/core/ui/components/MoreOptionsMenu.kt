package com.example.pulsemusic.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

data class MenuItemAction(
    val text: String,
    val onClick: () -> Unit,
    val color: Color = Color.Unspecified,
    val enabled: Boolean = true
)

// Компонент меню елементу в списку пісень
@Composable
fun MoreOptionsMenu(
    items: List<MenuItemAction>,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.Gray,
    contentDescription: String = "Опції",
    onOpen: (() -> Unit)? = null
) {
    MoreOptionsMenu(
        modifier = modifier,
        iconTint = iconTint,
        contentDescription = contentDescription,
        onOpen = onOpen
    ) { dismiss ->
        items.forEach { menuItem ->
            DropdownMenuItem(
                text = { Text(menuItem.text, color = menuItem.color) },
                onClick = {
                    dismiss()
                    menuItem.onClick()
                },
                enabled = menuItem.enabled
            )
        }
    }
}

@Composable
fun MoreOptionsMenu(
    modifier: Modifier = Modifier,
    iconTint: Color = Color.Gray,
    contentDescription: String = "Опції",
    onOpen: (() -> Unit)? = null,
    menuContent: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = {
                menuExpanded = true
                onOpen?.invoke()
            }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = contentDescription,
                tint = iconTint
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            menuContent { menuExpanded = false }
        }
    }
}
