package com.example.pulsemusic.core.data

import com.example.pulsemusic.core.data.model.CategoryListResponse
import com.example.pulsemusic.core.database.dao.CategoryDao
import com.example.pulsemusic.core.database.mapper.toDomain
import com.example.pulsemusic.core.database.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface CoreRepository {
    suspend fun toggleLike(songId: String): Boolean
    suspend fun refreshCategories(): Result<Unit>
    fun observeCategories(): Flow<List<CategoryListResponse>>
}

// Загально використовувані функції запитів
class CoreRepositoryImpl(
    private val coreApiService: CoreApiService,
    private val authPreferences: AuthPreferences,
    private val categoryDao: CategoryDao
) : CoreRepository {

    private fun tokenProvider() = authPreferences.accessToken.value?.let { "Bearer $it" }

    override suspend fun toggleLike(songId: String): Boolean {
        val token = tokenProvider() ?: return false
        return try {
            val response = coreApiService.toggleLike(token, songId)
            if (response.isSuccessful) {
                response.body()?.liked ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun observeCategories(): Flow<List<CategoryListResponse>> {
        return categoryDao.observeCategories().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun refreshCategories(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = coreApiService.getCategories()
            if (response.isSuccessful) {
                val categories = response.body() ?: emptyList()
                categoryDao.insertCategories(categories.map { it.toEntity() })
                Result.success(Unit)
            } else {
                Result.failure(Exception("Помилка отримання категорій: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
