package com.example.pulsemusic.feature.auth.data

import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.feature.auth.data.model.AuthCredentials
import com.example.pulsemusic.feature.auth.data.model.LogoutRequest

interface AuthRepository {
    suspend fun login(login: String, password: String): Result<Unit>
    suspend fun register(login: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}

// Репозиторій автентифікації
class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val authPrefs: AuthPreferences
) : AuthRepository {

    override suspend fun login(login: String, password: String): Result<Unit> {
        return try {
            val res = apiService.login(AuthCredentials(login, password))
            if (res.isSuccessful) {
                res.body()?.let {
                    authPrefs.saveTokens(it.accessToken, it.refreshToken)
                    authPrefs.saveUsername(login)
                    Result.success(Unit)
                } ?: Result.failure(Exception("Порожня відповідь сервера"))
            } else {
                Result.failure(Exception("Помилка входу: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Помилка підключення до сервера"))
        }
    }

    override suspend fun register(login: String, password: String): Result<Unit> {
        return try {
            val res = apiService.register(AuthCredentials(login, password))
            if (res.isSuccessful) {
                res.body()?.let {
                    authPrefs.saveTokens(it.accessToken, it.refreshToken)
                    authPrefs.saveUsername(login)
                    Result.success(Unit)
                } ?: Result.failure(Exception("Порожня відповідь сервера"))
            } else {
                Result.failure(Exception("Помилка реєстрації: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Помилка підключення до сервера"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val refresh = authPrefs.getRefreshTokenSync()
            if (refresh != null) {
                val res = apiService.logout(LogoutRequest(refresh))
                if (res.isSuccessful) {
                    authPrefs.clearAuth()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Помилка виходу: ${res.code()}"))
                }
            } else {
                authPrefs.clearAuth()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Помилка підключення до сервера"))
        }
    }
}
