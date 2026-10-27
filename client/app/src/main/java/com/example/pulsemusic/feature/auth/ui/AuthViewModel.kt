package com.example.pulsemusic.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.core.util.AppToastManager
import com.example.pulsemusic.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isRegisterTab: Boolean = false,
    val loginText: String = "",
    val passwordText: String = "",
    val confirmPasswordText: String = "",
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
)

// Модель представлення автентифікації
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    authPrefs: AuthPreferences
) : ViewModel() {
    val accessToken = authPrefs.accessToken

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onTabSelected(isRegister: Boolean) {
        _uiState.update {
            it.copy(
                isRegisterTab = isRegister,
                errorMessage = null,
            )
        }
    }

    fun onLoginChanged(text: String) {
        _uiState.update {
            it.copy(
                loginText = text,
                errorMessage = null,
            )
        }
    }

    fun onPasswordChanged(text: String) {
        _uiState.update {
            it.copy(
                passwordText = text,
                errorMessage = null,
            )
        }
    }

    fun onConfirmPasswordChanged(text: String) {
        _uiState.update {
            it.copy(
                confirmPasswordText = text,
                errorMessage = null,
            )
        }
    }

    fun submit() {
        val currentState = _uiState.value
        val login = currentState.loginText.trim()
        val password = currentState.passwordText.trim()
        val confirmPassword = currentState.confirmPasswordText.trim()

        if (login.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Заповніть усі обов'язкові поля") }
            return
        }

        if (currentState.isRegisterTab && (password != confirmPassword)) {
            _uiState.update { it.copy(errorMessage = "Паролі не збігаються") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val result = if (currentState.isRegisterTab) {
                repository.register(login, password)
            } else {
                repository.login(login, password)
            }

            if (result.isSuccess) {
                AppToastManager.showMessage(if (currentState.isRegisterTab) "Успішна реєстрація" else "Успішний вхід")
                _uiState.update { it.copy(isSubmitting = false) }
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                val defaultError = if (currentState.isRegisterTab) "Помилка реєстрації" else "Помилка входу"
                AppToastManager.showMessage(errorMsg ?: defaultError)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = errorMsg ?: defaultError,
                    )
                }
            }
        }
    }

    fun logout(onResult: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.logout()
            if (result.isSuccess) {
                AppToastManager.showMessage("Ви вийшли з акаунта")
                onResult?.invoke(true, null)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Помилка виходу"
                AppToastManager.showMessage(errorMsg)
                onResult?.invoke(false, errorMsg)
            }
        }
    }
}
