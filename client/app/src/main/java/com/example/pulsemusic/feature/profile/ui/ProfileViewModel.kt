package com.example.pulsemusic.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.core.util.AppToastManager
import com.example.pulsemusic.feature.profile.data.ProfileRepository
import com.example.pulsemusic.feature.profile.data.model.UpdateUser
import com.example.pulsemusic.feature.profile.data.model.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userProfile: UserResponse? = null,
    val showEditDialog: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val editLoginText: String = "",
    val currentPasswordText: String = "",
    val newPasswordText: String = "",
    val confirmPasswordText: String = "",
    val isUpdatingProfile: Boolean = false,
    val isDeletingProfile: Boolean = false,
) {
    val isPasswordMismatch: Boolean
        get() = (newPasswordText.isNotBlank() || confirmPasswordText.isNotBlank()) && (newPasswordText != confirmPasswordText)

    val isLoginChanged: Boolean
        get() = editLoginText.isNotBlank() && (editLoginText != userProfile?.login)

    val isPasswordChanging: Boolean
        get() = newPasswordText.isNotBlank() || confirmPasswordText.isNotBlank()

    val hasChanges: Boolean
        get() = isLoginChanged || isPasswordChanging

    val isPasswordValid: Boolean
        get() = !isPasswordChanging || (currentPasswordText.isNotBlank() && (newPasswordText == confirmPasswordText))

    val canSaveProfile: Boolean
        get() = hasChanges && isPasswordValid && !isUpdatingProfile
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val authPrefs: AuthPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }

        viewModelScope.launch {
            authPrefs.userId.collect { userId ->
                if (!userId.isNullOrBlank()) {
                    loadProfile(userId)
                }
            }
        }
    }

    fun updateProfile() {
        val currentState = _uiState.value
        if (!currentState.canSaveProfile) return

        val loginToUpdate = currentState.editLoginText.takeIf { currentState.isLoginChanged }
        val passwordToUpdate = currentState.newPasswordText.takeIf { currentState.isPasswordChanging }

        _uiState.update { it.copy(isUpdatingProfile = true) }

        viewModelScope.launch {
            val request = UpdateUser(
                login = loginToUpdate,
                currentPassword = currentState.currentPasswordText,
                password = passwordToUpdate,
            )
            val result = repository.updateProfile(request)
            if (result.isSuccess) {
                AppToastManager.showMessage("Профіль оновлено")
                _uiState.update {
                    it.copy(
                        isUpdatingProfile = false,
                        showEditDialog = false,
                    )
                }
            } else {
                AppToastManager.showMessage(result.exceptionOrNull()?.message ?: "Помилка оновлення")
                _uiState.update { it.copy(isUpdatingProfile = false) }
            }
        }
    }

    fun deleteProfile(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isDeletingProfile = true, showDeleteDialog = false) }
        viewModelScope.launch {
            val result = repository.deleteProfile()
            if (result.isSuccess) {
                AppToastManager.showMessage("Профіль видалено")
                _uiState.update { it.copy(isDeletingProfile = false) }
                onSuccess()
            } else {
                AppToastManager.showMessage(result.exceptionOrNull()?.message ?: "Помилка видалення")
                _uiState.update { it.copy(isDeletingProfile = false) }
            }
        }
    }

    fun openEditDialog() {
        val currentLogin = _uiState.value.userProfile?.login ?: ""
        _uiState.update {
            it.copy(
                showEditDialog = true,
                editLoginText = currentLogin,
                currentPasswordText = "",
                newPasswordText = "",
                confirmPasswordText = "",
            )
        }
    }

    fun closeEditDialog() {
        _uiState.update { it.copy(showEditDialog = false) }
    }

    fun setLogoutDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showLogoutDialog = visible) }
    }

    fun setDeleteDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = visible) }
    }

    fun onEditLoginChanged(text: String) {
        _uiState.update { it.copy(editLoginText = text) }
    }

    fun onCurrentPasswordChanged(text: String) {
        _uiState.update { it.copy(currentPasswordText = text) }
    }

    fun onNewPasswordChanged(text: String) {
        _uiState.update { it.copy(newPasswordText = text) }
    }

    fun onConfirmPasswordChanged(text: String) {
        _uiState.update { it.copy(confirmPasswordText = text) }
    }

    private fun loadProfile(userId: String) {
        viewModelScope.launch {
            val result = repository.refreshProfile(userId)
            if (result.isFailure) {
                AppToastManager.showMessage("Не вдалося завантажити профіль")
            }
        }
    }
}
