package com.example.pulsemusic.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pulsemusic.core.ui.components.MainTopBar
import com.example.pulsemusic.core.ui.theme.YtDarkBackground
import com.example.pulsemusic.feature.library.ui.components.LibraryTabRow
import com.example.pulsemusic.feature.library.ui.tabs.downloads.DownloadsTabContent
import com.example.pulsemusic.feature.library.ui.tabs.downloads.DownloadsViewModel
import com.example.pulsemusic.feature.library.ui.tabs.history.HistoryTabContent
import com.example.pulsemusic.feature.library.ui.tabs.history.HistoryViewModel
import com.example.pulsemusic.feature.library.ui.tabs.likes.LikesTabContent
import com.example.pulsemusic.feature.library.ui.tabs.likes.LikesViewModel
import com.example.pulsemusic.feature.library.ui.tabs.mysongs.MySongsTabContent
import com.example.pulsemusic.feature.library.ui.tabs.mysongs.MySongsViewModel
import com.example.pulsemusic.feature.library.ui.tabs.playlists.PlaylistsTabContent
import com.example.pulsemusic.feature.library.ui.tabs.playlists.PlaylistsViewModel
import com.example.pulsemusic.feature.player.ui.PlayerViewModel

// Екран бібліотеки
@Composable
fun LibraryScreen(
    playerViewModel: PlayerViewModel,
    onNavigateToExplore: ((String, String?) -> Unit)? = null,
    sharedViewModel: LibraryViewModel = hiltViewModel()
) {
    val sharedState by sharedViewModel.uiState.collectAsState()
    val accessToken by sharedViewModel.accessToken.collectAsState()

    val tabs = listOf("Історія", "Вподобане", "Плейлисти", "Завантаження", "Мої пісні")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YtDarkBackground)
    ) {
        MainTopBar(
            title = "Бібліотека",
            onSearch = { sharedViewModel.setSearchQuery(it) }
        )

        LibraryTabRow(
            tabs = tabs,
            selectedTabIndex = sharedState.selectedTabIndex,
            onTabSelected = { sharedViewModel.setTab(it) }
        )

        if (accessToken.isNullOrBlank() && sharedState.selectedTabIndex != 3) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Будь ласка, увійдіть в акаунт, щоб переглядати бібліотеку",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                LaunchedEffect(maxHeight) {
                    sharedViewModel.onContainerHeightChanged(maxHeight.value)
                }
                when (sharedState.selectedTabIndex) {
                    0 -> {
                        val historyViewModel: HistoryViewModel = hiltViewModel()
                        HistoryTabContent(
                            viewModel = historyViewModel,
                            searchQuery = sharedState.searchQuery,
                            pageSize = sharedState.pageSize,
                            playerViewModel = playerViewModel,
                            onAuthorClick = onNavigateToExplore
                        )
                    }
                    1 -> {
                        val likesViewModel: LikesViewModel = hiltViewModel()
                        LikesTabContent(
                            viewModel = likesViewModel,
                            searchQuery = sharedState.searchQuery,
                            pageSize = sharedState.pageSize,
                            playerViewModel = playerViewModel,
                            onAuthorClick = onNavigateToExplore
                        )
                    }
                    2 -> {
                        val playlistsViewModel: PlaylistsViewModel = hiltViewModel()
                        PlaylistsTabContent(
                            viewModel = playlistsViewModel,
                            searchQuery = sharedState.searchQuery,
                            playerViewModel = playerViewModel,
                            onAuthorClick = onNavigateToExplore
                        )
                    }
                    3 -> {
                        val downloadsViewModel: DownloadsViewModel = hiltViewModel()
                        DownloadsTabContent(
                            viewModel = downloadsViewModel,
                            searchQuery = sharedState.searchQuery,
                            playerViewModel = playerViewModel,
                            onAuthorClick = onNavigateToExplore
                        )
                    }
                    4 -> {
                        val mySongsViewModel: MySongsViewModel = hiltViewModel()
                        MySongsTabContent(
                            viewModel = mySongsViewModel,
                            searchQuery = sharedState.searchQuery,
                            pageSize = sharedState.pageSize,
                            playerViewModel = playerViewModel,
                            onAuthorClick = onNavigateToExplore
                        )
                    }
                }
            }
        }
    }
}