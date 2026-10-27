package com.example.pulsemusic.core.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pulsemusic.feature.auth.ui.AuthScreen
import com.example.pulsemusic.feature.auth.ui.AuthViewModel
import com.example.pulsemusic.feature.player.data.model.PlayerState
import com.example.pulsemusic.feature.player.ui.PlayerComponent
import com.example.pulsemusic.feature.player.ui.PlayerViewModel
import com.example.pulsemusic.feature.explore.ui.ExploreScreen
import com.example.pulsemusic.feature.explore.ui.ExploreViewModel
import com.example.pulsemusic.feature.library.ui.LibraryScreen
import com.example.pulsemusic.feature.profile.ui.ProfileScreen
import com.example.pulsemusic.core.util.AppToastManager
import com.example.pulsemusic.core.ui.navigation.AppBottomNavigationBar
import com.example.pulsemusic.core.ui.navigation.AppDestination
import com.example.pulsemusic.core.ui.theme.YtDarkBackground

// Основний каркас
@Composable
fun MainAppScaffold(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    exploreViewModel: ExploreViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Стан плеєра: COMPACT (міні-плеєр знизу) або FULL (на весь екран)
    var playerState by remember { mutableStateOf(PlayerState.COMPACT) }

    // Відстеження поточного треку та стану авторизації
    val currentSong by playerViewModel.currentSong.collectAsState()
    val accessToken by authViewModel.accessToken.collectAsState()
    
    // Сповіщання плеєра про зміну токена авторизації
    LaunchedEffect(accessToken) {
        playerViewModel.onAuthChanged()
    }

    // Якщо трек відсутній, автоматично повертаємо плеєр у компактний стан
    LaunchedEffect(currentSong) {
        if (currentSong == null) {
            playerState = PlayerState.COMPACT
        }
    }

    // Слухаємо глобальні Toast-повідомлення та відображаємо їх
    LaunchedEffect(Unit) {
        AppToastManager.messages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Перехоплюємо системну кнопку "Назад", коли відкритий повноформатний плеєр
    BackHandler(enabled = playerState == PlayerState.FULL) {
        playerState = PlayerState.COMPACT
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = YtDarkBackground,
            bottomBar = {
                Column {
                    // Компактний міні-плеєр над нижнім меню навігації
                    if (currentSong != null && playerState == PlayerState.COMPACT) {
                        PlayerComponent(
                            state = PlayerState.COMPACT,
                            viewModel = playerViewModel,
                            onExpand = { playerState = PlayerState.FULL },
                            onCollapse = { playerState = PlayerState.COMPACT }
                        )
                    }

                    // Нижня панель навігації
                    AppBottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Основний граф навігації додатка
                NavHost(
                    navController = navController,
                    startDestination = AppDestination.EXPLORE.route
                ) {
                    // Екран "Головна" (Explore)
                    composable(AppDestination.EXPLORE.route) {
                        ExploreScreen(
                            playerViewModel = playerViewModel,
                            exploreViewModel = exploreViewModel
                        )
                    }

                    // Екран "Медіатека" (Library)
                    composable(AppDestination.LIBRARY.route) {
                        LibraryScreen(
                            playerViewModel = playerViewModel,
                            onNavigateToExplore = { authorId, authorName ->
                                exploreViewModel.onAuthorFilterChanged(authorId, authorName)
                                if (navController.currentDestination?.route != AppDestination.EXPLORE.route) {
                                    navController.navigate(AppDestination.EXPLORE.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }

                    // Екран "Акаунт / Авторизація" (Profile / Auth)
                    composable(AppDestination.PROFILE.route) {
                        if (accessToken.isNullOrBlank()) {
                            AuthScreen(
                                viewModel = authViewModel,
                                onContinueAsGuest = {
                                    if (navController.currentDestination?.route != AppDestination.EXPLORE.route) {
                                        navController.navigate(AppDestination.EXPLORE.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        } else {
                            ProfileScreen(authViewModel = authViewModel)
                        }
                    }
                }
            }
        }

        // Повноформатний плеєр
        AnimatedVisibility(
            visible = playerState == PlayerState.FULL && currentSong != null,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            PlayerComponent(
                state = PlayerState.FULL,
                viewModel = playerViewModel,
                onExpand = { playerState = PlayerState.FULL },
                onCollapse = { playerState = PlayerState.COMPACT },
                modifier = Modifier.fillMaxSize(),
                onAuthorClick = { authorId, authorName ->
                    playerState = PlayerState.COMPACT
                    exploreViewModel.onAuthorFilterChanged(authorId, authorName)
                    if (navController.currentDestination?.route != AppDestination.EXPLORE.route) {
                        navController.navigate(AppDestination.EXPLORE.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
