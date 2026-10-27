package com.example.pulsemusic.feature.library.ui.tabs.mysongs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.example.pulsemusic.core.data.model.CategoryListResponse
import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.ui.components.AppDialog
import com.example.pulsemusic.core.ui.components.AppTextField
import com.example.pulsemusic.core.ui.components.FilterChipRow
import com.example.pulsemusic.core.util.extractSongMetadata
import com.example.pulsemusic.feature.library.ui.components.DottedUploadBox
import kotlinx.coroutines.launch

// Діалог додавання/редагування пісні
@Composable
fun MySongDialog(
    item: SongRankingItem? = null,
    categories: List<CategoryListResponse>,
    onDismissRequest: () -> Unit,
    onConfirmLocal: (title: String, description: String?, originalAuthor: String?, categoryIds: List<String>?, songUri: Uri?, coverUri: Uri?) -> Unit,
    onConfirmExternal: (externalUrl: String, categoryIds: List<String>?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isEdit = item != null
    var isExternal by remember { mutableStateOf(false) }
    var editCategories by remember { mutableStateOf(!isEdit) }

    var title by remember { mutableStateOf(item?.title ?: "") }
    var description by remember { mutableStateOf("") }
    var originalAuthor by remember { mutableStateOf(item?.originalAuthor ?: "") }
    var selectedCategoryIds by remember { mutableStateOf(setOf<String>()) }
    var songUri by remember { mutableStateOf<Uri?>(null) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }

    var externalUrl by remember { mutableStateOf("") }

    var showExtractDialog by remember { mutableStateOf(false) }
    var pendingSongUri by remember { mutableStateOf<Uri?>(null) }

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pendingSongUri = uri
            showExtractDialog = true
        }
    }
    
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) coverUri = uri
    }

    val sortedCategories = remember(categories, selectedCategoryIds) {
        categories.sortedByDescending { selectedCategoryIds.contains(it.id) }
    }
    
    val screenHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }

    if (showExtractDialog && pendingSongUri != null) {
        AlertDialog(
            onDismissRequest = {
                songUri = pendingSongUri
                showExtractDialog = false
                pendingSongUri = null
            },
            title = { Text("Витягнути метадані?", color = Color.White) },
            text = { Text("Спробувати витягнути назву, автора та обкладинку з вибраного аудіофайлу?", color = Color.White) },
            containerColor = Color(0xFF282828),
            confirmButton = {
                TextButton(onClick = {
                    val uriToExtract = pendingSongUri
                    if (uriToExtract != null) {
                        coroutineScope.launch {
                            val metadata = extractSongMetadata(context, uriToExtract)
                            if (!metadata.title.isNullOrBlank()) title = metadata.title
                            if (!metadata.author.isNullOrBlank()) originalAuthor = metadata.author
                            if (metadata.coverUri != null) coverUri = metadata.coverUri
                            songUri = uriToExtract
                            showExtractDialog = false
                            pendingSongUri = null
                        }
                    }
                }) {
                    Text("Так", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    songUri = pendingSongUri
                    showExtractDialog = false
                    pendingSongUri = null
                }) {
                    Text("Ні", color = Color.Gray)
                }
            }
        )
    }

    AppDialog(
        onDismissRequest = onDismissRequest,
        title = if (isEdit) "Редагувати пісню" else "Створити пісню",
        confirmButtonText = if (isEdit) "Зберегти" else "Створити",
        confirmButtonEnabled = if (isExternal) externalUrl.isNotBlank() else title.isNotBlank() && (isEdit || songUri != null),
        onConfirm = {
            val categoriesToSend = if (editCategories) selectedCategoryIds.toList() else null
            if (isExternal) {
                onConfirmExternal(externalUrl, categoriesToSend)
            } else {
                onConfirmLocal(title, description.takeIf { it.isNotBlank() }, originalAuthor.takeIf { it.isNotBlank() }, categoriesToSend, songUri, coverUri)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.6f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isExternal) {
                AppTextField(
                    value = externalUrl,
                    onValueChange = { input ->
                        val urlRegex = "https?://\\S+".toRegex()
                        val match = urlRegex.find(input)
                        var extractedUrl = match?.value ?: input
                        while (extractedUrl.isNotEmpty() && !extractedUrl.last().isLetterOrDigit() && extractedUrl.last() != '/') {
                            extractedUrl = extractedUrl.dropLast(1)
                        }
                        externalUrl = extractedUrl
                    },
                    label = "URL пісні з Audius"
                )
            } else {
                AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Назва"
                )
                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Опис (необов'язково)"
                )
                AppTextField(
                    value = originalAuthor,
                    onValueChange = { originalAuthor = it },
                    label = "Оригінальний автор (необов'язково)"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DottedUploadBox(
                        text = if (songUri != null) "Аудіо вибрано" else if (isEdit) "Аудіо (необов'язково)" else "Аудіо",
                        icon = Icons.Default.MusicNote,
                        isSelected = songUri != null,
                        onClick = { audioLauncher.launch("audio/*") },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )

                    DottedUploadBox(
                        text = if (coverUri != null) "Обкладинку вибрано" else if (isEdit) "Обкладинка (необов'язково)" else "Обкладинка",
                        icon = Icons.Default.Image,
                        isSelected = coverUri != null,
                        onClick = { imageLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = isExternal,
                    onCheckedChange = { isExternal = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFCC00FF)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = "Audius Logo",
                    tint = Color(0xFFCC00FF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Посилання з Audius", color = Color.White)
            }

            if (isEdit) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = editCategories,
                            onCheckedChange = { editCategories = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFCC00FF)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Змінити категорії", color = Color.White)
                    }

                    if (editCategories && selectedCategoryIds.isNotEmpty()) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${selectedCategoryIds.size} вибрано",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Категорії", color = Color.White)

                    if (selectedCategoryIds.isNotEmpty()) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${selectedCategoryIds.size} вибрано",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (editCategories) {
                FilterChipRow(
                    items = sortedCategories,
                    labelProvider = { it.title },
                    isSelected = { selectedCategoryIds.contains(it.id) },
                    onItemClick = { cat ->
                        selectedCategoryIds = if (selectedCategoryIds.contains(cat.id)) {
                            selectedCategoryIds - cat.id
                        } else {
                            selectedCategoryIds + cat.id
                        }
                    },
                    keyProvider = { it.id }
                )
            }
        }
    }
}