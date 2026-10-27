package com.example.pulsemusic.feature.player.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.pulsemusic.feature.player.data.model.PlayerSource
import com.example.pulsemusic.feature.player.data.model.PlayerState
import com.example.pulsemusic.feature.player.ui.components.PlaylistSheet
import com.example.pulsemusic.feature.player.ui.components.QueueSheet

// Головний компонент плеєру
@Composable
fun PlayerComponent(
    state: PlayerState,
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    onExpand: () -> Unit = {},
    onCollapse: () -> Unit = {},
    onAuthorClick: ((String, String?) -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showQueueSheet) {
        QueueSheet(
            queueSongs = uiState.queueSongs,
            currentQueueIndex = uiState.currentQueueIndex,
            onDismissRequest = viewModel::closeQueueSheet,
            onPlayQueueSongAt = viewModel::playQueueSongAt,
            onRemoveFromQueue = viewModel::removeFromQueue,
        )
    }

    if (uiState.showPlaylistSheet) {
        PlaylistSheet(
            userPlaylists = uiState.userPlaylists,
            onDismissRequest = viewModel::closePlaylistSheet,
        ) { playlistId, callback ->
            viewModel.toggleSongInPlaylist(playlistId = playlistId, onResult = callback)
        }
    }

    val song = uiState.currentSong ?: return

    if (state == PlayerState.COMPACT) {
        MiniPlayer(
            song = song,
            isPlaying = uiState.isPlaying,
            currentPositionMs = uiState.currentPositionMs,
            durationMs = uiState.durationMs,
            onTogglePlayPause = viewModel::togglePlayPause,
            onNext = viewModel::playNext,
            onPrevious = viewModel::playPrevious,
            onExpand = onExpand,
            isBuffering = uiState.isBuffering,
            modifier = modifier,
        )
    } else {
        FullPlayer(
            song = song,
            isPlaying = uiState.isPlaying,
            currentPositionMs = uiState.currentPositionMs,
            durationMs = uiState.durationMs,
            isFavorite = song.hasLiked,
            isDownloaded = uiState.isCurrentSongDownloaded,
            isDownloading = uiState.isDownloading,
            isLocalSource = uiState.currentSource == PlayerSource.LOCAL,
            onTogglePlayPause = viewModel::togglePlayPause,
            onSeekTo = viewModel::seekTo,
            onNext = viewModel::playNext,
            onPrevious = viewModel::playPrevious,
            onToggleFavorite = viewModel::toggleLike,
            onCollapse = onCollapse,
            onAddToPlaylist = viewModel::onAddToPlaylistClicked,
            onOpenQueue = viewModel::openQueueSheet,
            onDownloadSong = viewModel::downloadSong,
            onDeleteSong = viewModel::deleteSong,
            allCategories = uiState.categories,
            onAuthorClick = onAuthorClick,
        )
    }
}
