package com.example.pulsemusic.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pulsemusic.core.ui.components.AppTextField
import com.example.pulsemusic.core.ui.theme.YtDarkBackground
import com.example.pulsemusic.core.ui.theme.YtDarkSurfaceVariant
import com.example.pulsemusic.core.ui.theme.YtRedAccent
import com.example.pulsemusic.core.ui.theme.YtTextSecondary

// Екран автентифікації, ділить вкладку з екраном профілю
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onContinueAsGuest: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YtDarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = YtDarkSurfaceVariant),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PrimaryTabRow(
                    selectedTabIndex = if (uiState.isRegisterTab) 1 else 0,
                    containerColor = YtDarkSurfaceVariant,
                    contentColor = YtRedAccent,
                ) {
                    Tab(
                        selected = !uiState.isRegisterTab,
                        onClick = { viewModel.onTabSelected(isRegister = false) },
                        text = { Text("Вхід", color = if (!uiState.isRegisterTab) Color.White else YtTextSecondary) },
                    )
                    Tab(
                        selected = uiState.isRegisterTab,
                        onClick = { viewModel.onTabSelected(isRegister = true) },
                        text = { Text("Реєстрація", color = if (uiState.isRegisterTab) Color.White else YtTextSecondary) },
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    value = uiState.loginText,
                    onValueChange = viewModel::onLoginChanged,
                    label = "Логін",
                    focusedBorderColor = YtRedAccent,
                    unfocusedBorderColor = Color(0xFF383838),
                    labelColor = YtTextSecondary,
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = uiState.passwordText,
                    onValueChange = viewModel::onPasswordChanged,
                    label = "Пароль",
                    visualTransformation = PasswordVisualTransformation(),
                    focusedBorderColor = YtRedAccent,
                    unfocusedBorderColor = Color(0xFF383838),
                    labelColor = YtTextSecondary,
                )

                if (uiState.isRegisterTab) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val isPasswordMismatch = uiState.passwordText.isNotBlank() &&
                            uiState.confirmPasswordText.isNotBlank() &&
                            (uiState.passwordText != uiState.confirmPasswordText)

                    AppTextField(
                        value = uiState.confirmPasswordText,
                        onValueChange = viewModel::onConfirmPasswordChanged,
                        label = "Підтвердіть пароль",
                        visualTransformation = PasswordVisualTransformation(),
                        focusedBorderColor = if (isPasswordMismatch) MaterialTheme.colorScheme.error else YtRedAccent,
                        unfocusedBorderColor = if (isPasswordMismatch) MaterialTheme.colorScheme.error else Color(0xFF383838),
                        labelColor = if (isPasswordMismatch) MaterialTheme.colorScheme.error else YtTextSecondary,
                    )

                    if (isPasswordMismatch) {
                        Text(
                            text = "Паролі не збігаються",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 4.dp, start = 8.dp),
                        )
                    }
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = viewModel::submit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSubmitting,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = YtRedAccent),
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                        )
                    } else {
                        Text(
                            text = if (uiState.isRegisterTab) "Зареєструватися" else "Увійти",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onContinueAsGuest) {
                Text(
                    text = "Продовжити як гість (офлайн)",
                    color = YtTextSecondary,
                )
            }
        }
    }
}
