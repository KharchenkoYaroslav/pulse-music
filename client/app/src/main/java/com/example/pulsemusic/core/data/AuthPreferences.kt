package com.example.pulsemusic.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.pulsemusic.core.util.getUserIdFromToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TokenProvider {
    fun provideRefreshToken(): String?
    fun provideUserId(): String?
    fun onTokensRefreshed(newAccessToken: String, newRefreshToken: String)
    fun onSessionExpired()
}

// Зберігання токенів авторизації, даних користувача та управління сесією

class AuthPreferences(context: Context) : TokenProvider {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _accessToken = MutableStateFlow(prefs.getString(KEY_ACCESS_TOKEN, null))
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _username = MutableStateFlow(prefs.getString(KEY_USERNAME, null))
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _userId = MutableStateFlow(getUserIdFromToken(_accessToken.value))
    val userId: StateFlow<String?> = _userId.asStateFlow()

    fun saveTokens(access: String, refresh: String) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, access)
            putString(KEY_REFRESH_TOKEN, refresh)
        }
        _accessToken.value = access
        _userId.value = getUserIdFromToken(access)
    }

    fun saveUsername(name: String) {
        prefs.edit { putString(KEY_USERNAME, name) }
        _username.value = name
    }

    fun clearAuth() {
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USERNAME)
        }
        _accessToken.value = null
        _username.value = null
        _userId.value = null
    }

    override fun provideRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    override fun provideUserId(): String? {
        return _userId.value
    }

    override fun onTokensRefreshed(newAccessToken: String, newRefreshToken: String) {
        saveTokens(newAccessToken, newRefreshToken)
    }

    override fun onSessionExpired() {
        clearAuth()
    }

    fun getRefreshTokenSync(): String? {
        return provideRefreshToken()
    }

    fun isLoggedIn(): Boolean {
        return !_accessToken.value.isNullOrBlank()
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USERNAME = "username"
    }
}
