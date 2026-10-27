package com.example.pulsemusic.feature.profile.data

import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.feature.profile.data.model.UpdateUser
import com.example.pulsemusic.feature.profile.data.model.UserResponse
import com.example.pulsemusic.core.database.dao.ProfileDao
import com.example.pulsemusic.core.database.mapper.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ProfileRepository {
    fun observeProfile(): Flow<UserResponse?>
    suspend fun refreshProfile(id: String): Result<Unit>
    suspend fun updateProfile(request: UpdateUser): Result<Unit>
    suspend fun deleteProfile(): Result<Unit>
}

// Репозиторій профілів

class ProfileRepositoryImpl(
    private val apiService: ProfileApiService,
    private val authPrefs: AuthPreferences,
    private val profileDao: ProfileDao
) : ProfileRepository {

    override fun observeProfile(): Flow<UserResponse?> {
        return profileDao.observeProfile().map { it?.toDomain() }
    }

    override suspend fun refreshProfile(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile(id)
            if (response.isSuccessful && response.body() != null) {
                profileDao.saveProfile(response.body()!!.toEntity())
                Result.success(Unit)
            } else {
                Result.failure(Exception("Помилка отримання профілю: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(request: UpdateUser): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = "Bearer ${authPrefs.accessToken.value}"
            val response = apiService.updateProfile(token, request)
            if (response.isSuccessful && response.body() != null) {
                response.body()?.login?.let { newUsername ->
                    authPrefs.saveUsername(newUsername)
                }
                profileDao.saveProfile(response.body()!!.toEntity())
                Result.success(Unit)
            } else {
                Result.failure(Exception("Помилка оновлення профілю: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProfile(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = "Bearer ${authPrefs.accessToken.value}"
            val response = apiService.deleteProfile(token)
            if (response.isSuccessful) {
                authPrefs.clearAuth()
                profileDao.clearProfile()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Помилка видалення профілю: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
