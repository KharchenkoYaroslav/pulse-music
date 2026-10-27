package com.example.pulsemusic.core.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pulsemusic.core.ui.theme.YtDarkSurface
import com.example.pulsemusic.core.ui.theme.YtDarkSurfaceVariant
import com.example.pulsemusic.core.ui.theme.YtTextSecondary


// Компонент нижньої навігаційної панелі додатка

@Composable
fun AppBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = YtDarkSurface,
        tonalElevation = 8.dp
    ) {
        // Вкладка "Головна"
        NavigationBarItem(
            selected = currentRoute == AppDestination.EXPLORE.route,
            onClick = {
                navController.navigate(AppDestination.EXPLORE.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Explore") },
            label = { Text("Головна") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = YtTextSecondary,
                unselectedTextColor = YtTextSecondary,
                indicatorColor = YtDarkSurfaceVariant
            )
        )

        // Вкладка "Медіатека"
        NavigationBarItem(
            selected = currentRoute == AppDestination.LIBRARY.route,
            onClick = {
                navController.navigate(AppDestination.LIBRARY.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
            label = { Text("Медіатека") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = YtTextSecondary,
                unselectedTextColor = YtTextSecondary,
                indicatorColor = YtDarkSurfaceVariant
            )
        )

        // Вкладка "Акаунт"
        NavigationBarItem(
            selected = currentRoute == AppDestination.PROFILE.route,
            onClick = {
                navController.navigate(AppDestination.PROFILE.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Акаунт") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = YtTextSecondary,
                unselectedTextColor = YtTextSecondary,
                indicatorColor = YtDarkSurfaceVariant
            )
        )
    }
}
