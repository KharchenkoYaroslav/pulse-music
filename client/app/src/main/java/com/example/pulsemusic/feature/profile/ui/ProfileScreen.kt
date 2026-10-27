package com.example.pulsemusic.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pulsemusic.core.ui.components.AppDialog
import com.example.pulsemusic.core.ui.theme.YtDarkBackground
import com.example.pulsemusic.core.ui.theme.YtDarkSurface
import com.example.pulsemusic.core.ui.theme.YtRedAccent
import com.example.pulsemusic.core.util.formatIsoDate
import com.example.pulsemusic.feature.auth.ui.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by profileViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YtDarkBackground)
            .padding(16.dp),
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = YtDarkSurface),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Логін:  ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = uiState.userProfile?.login ?: "Завантаження...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { profileViewModel.openEditDialog() },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = YtDarkSurface),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Створено:  ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                val formattedDate = remember(uiState.userProfile?.createdAt) {
                    formatIsoDate(uiState.userProfile?.createdAt)
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { profileViewModel.setLogoutDialogVisible(visible = true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = YtRedAccent),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                "Logout",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { profileViewModel.setDeleteDialogVisible(visible = true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = YtRedAccent),
            shape = RoundedCornerShape(8.dp),
            enabled = !uiState.isDeletingProfile,
        ) {
            Text(
                "Delete Account",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }

    if (uiState.showEditDialog) {
        EditProfileDialog(
            uiState = uiState,
            onDismiss = profileViewModel::closeEditDialog,
            onLoginChange = profileViewModel::onEditLoginChanged,
            onCurrentPasswordChange = profileViewModel::onCurrentPasswordChanged,
            onNewPasswordChange = profileViewModel::onNewPasswordChanged,
            onConfirmPasswordChange = profileViewModel::onConfirmPasswordChanged,
            onSave = profileViewModel::updateProfile,
        )
    }

    if (uiState.showLogoutDialog) {
        AppDialog(
            onDismissRequest = { profileViewModel.setLogoutDialogVisible(visible = false) },
            title = "Вихід",
            confirmButtonText = "Так",
            confirmButtonColor = YtRedAccent,
            onConfirm = {
                profileViewModel.setLogoutDialogVisible(visible = false)
                authViewModel.logout()
            },
        ) {
            Text(
                "Ви впевнені, що хочете вийти з акаунту?",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    if (uiState.showDeleteDialog) {
        AppDialog(
            onDismissRequest = { profileViewModel.setDeleteDialogVisible(visible = false) },
            title = "Видалення акаунту",
            confirmButtonText = "Так, видалити",
            confirmButtonColor = YtRedAccent,
            onConfirm = {
                profileViewModel.deleteProfile {
                    authViewModel.logout()
                }
            },
        ) {
            Text(
                "Ви впевнені? Цю дію неможливо скасувати.",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
