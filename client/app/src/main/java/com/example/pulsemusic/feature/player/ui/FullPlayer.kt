package com.example.pulsemusic.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pulsemusic.core.ui.theme.YtRedAccent
import com.example.pulsemusic.core.ui.theme.YtTextSecondary
import com.example.pulsemusic.core.data.model.CategoryListResponse
import com.example.pulsemusic.feature.player.data.model.Song
import com.example.pulsemusic.core.util.formatCount
import com.example.pulsemusic.core.util.formatTime
import com.example.pulsemusic.feature.player.ui.components.PlayerDescriptionSheet
import com.example.pulsemusic.feature.player.ui.components.PlayerMenuSheet
import com.example.pulsemusic.feature.player.ui.components.rememberAnimatedProgress
import kotlinx.coroutines.launch

// Повнорозмірний плеєр
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayer(
    song: Song,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isFavorite: Boolean,
    isDownloaded: Boolean = false,
    isDownloading: Boolean = false,
    isLocalSource: Boolean = false,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCollapse: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onOpenQueue: () -> Unit = {},
    onDownloadSong: () -> Unit = {},
    onDeleteSong: () -> Unit = {},
    allCategories: List<CategoryListResponse> = emptyList(),
    onAuthorClick: ((String, String?) -> Unit)? = null
) {
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    var showMenuSheet by remember { mutableStateOf(false) }
    var showDescriptionSheet by remember { mutableStateOf(false) }

    val songCategories = remember(song.categoryIds, allCategories) {
        if (song.categoryIds.isEmpty()) emptyList()
        else {
            val map = allCategories.associateBy { it.id }
            song.categoryIds.map { id ->
                map[id] ?: CategoryListResponse(id = id, title = id)
            }
        }
    }
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    val animatableProgress = rememberAnimatedProgress(
        currentPositionMs = currentPositionMs,
        durationMs = durationMs,
        isDraggingSlider = isDraggingSlider
    )

    val displayedProgress = if (isDraggingSlider) sliderValue else animatableProgress.value

    val ambientGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF282828),
            Color(0xFF141414),
            Color(0xFF030303)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ambientGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                IconButton(
                    onClick = onOpenQueue,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = { showMenuSheet = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (!isLocalSource) {
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = "Views",
                        tint = YtTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCount(song.viewsCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = YtTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        modifier = Modifier.clickable { onToggleFavorite() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Likes",
                            tint = if (isFavorite) YtRedAccent else YtTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatCount(song.likesCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = YtTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AsyncImage(
            model = song.cover,
            contentDescription = song.title,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.basicMarquee(spacing = MarqueeSpacing(16.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!song.originalAuthor.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Автор: ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 15.sp,
                            color = YtTextSecondary,
                        )
                        Text(
                            text = song.originalAuthor,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 15.sp,
                            color = YtTextSecondary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f).basicMarquee(spacing = MarqueeSpacing(12.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Публікатор: ",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 15.sp,
                        color = YtTextSecondary,
                    )
                    
                    val authorModifier = if (onAuthorClick != null) {
                        Modifier
                            .weight(1f, fill = false)
                            .clickable { onAuthorClick(song.authorId, song.authorLogin) }
                            .basicMarquee(spacing = MarqueeSpacing(12.dp))
                    } else {
                        Modifier
                            .weight(1f, fill = false)
                            .basicMarquee(spacing = MarqueeSpacing(12.dp))
                    }
                    
                    Text(
                        text = song.authorLogin ?: song.authorId,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 15.sp,
                        color = YtTextSecondary,
                        maxLines = 1,
                        modifier = authorModifier
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = displayedProgress,
                onValueChange = {
                    isDraggingSlider = true
                    sliderValue = it
                },
                onValueChangeFinished = {
                    val targetMs = (sliderValue * durationMs).toLong()
                    onSeekTo(targetMs)
                    coroutineScope.launch {
                        animatableProgress.snapTo(sliderValue)
                    }
                    isDraggingSlider = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = YtRedAccent,
                    inactiveTrackColor = Color(0xFF333333)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentSec = if (isDraggingSlider) {
                    ((sliderValue * durationMs) / 1000).toLong()
                } else {
                    currentPositionMs / 1000
                }
                val totalSec = durationMs / 1000

                Text(
                    text = formatTime(currentSec),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = YtTextSecondary
                )
                Text(
                    text = formatTime(totalSec),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = YtTextSecondary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(42.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(36.dp))

            FloatingActionButton(
                onClick = onTogglePlayPause,
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = Color.Black,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier.size(76.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.width(36.dp))

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(42.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showMenuSheet) {
        PlayerMenuSheet(
            onDismissRequest = { showMenuSheet = false },
            sheetState = sheetState,
            isLocalSource = isLocalSource,
            isDownloaded = isDownloaded,
            isDownloading = isDownloading,
            onOpenQueue = onOpenQueue,
            onAddToPlaylist = onAddToPlaylist,
            onDeleteSong = onDeleteSong,
            onDownloadSong = onDownloadSong,
            onShowDescription = { showDescriptionSheet = true }
        )
    }

    if (showDescriptionSheet) {
        PlayerDescriptionSheet(
            onDismissRequest = { showDescriptionSheet = false },
            sheetState = sheetState,
            description = song.description,
            categories = songCategories
        )
    }
}
