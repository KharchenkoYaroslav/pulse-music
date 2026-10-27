package com.example.pulsemusic.feature.profile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pulsemusic.core.ui.components.AppDialog
import com.example.pulsemusic.core.ui.components.AppTextField
import com.example.pulsemusic.core.ui.theme.YtRedAccent

@Composable
fun EditProfileDialog(
    uiState: ProfileUiState,
    onDismiss: () -> Unit,
    onLoginChange: (String) -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = "Редагувати профіль",
        confirmButtonText = "Зберегти",
        confirmButtonColor = YtRedAccent,
        confirmButtonEnabled = uiState.canSaveProfile,
        onConfirm = onSave,
    ) {
        Column {
            AppTextField(
                value = uiState.editLoginText,
                onValueChange = onLoginChange,
                label = "Новий логін",
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = uiState.currentPasswordText,
                onValueChange = onCurrentPasswordChange,
                label = "Поточний пароль",
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = uiState.newPasswordText,
                onValueChange = onNewPasswordChange,
                label = "Новий пароль",
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = uiState.confirmPasswordText,
                onValueChange = onConfirmPasswordChange,
                label = "Підтвердіть новий пароль",
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.isPasswordMismatch,
                supportingText = if (uiState.isPasswordMismatch) {
                    { Text("Паролі не збігаються", color = MaterialTheme.colorScheme.error) }
                } else null,
            )
        }
    }
}
