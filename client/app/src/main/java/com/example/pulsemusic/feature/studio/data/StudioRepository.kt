package com.example.pulsemusic.feature.studio.data

import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.feature.studio.data.model.CreateExternalSong
import com.example.pulsemusic.feature.studio.data.model.StudioIdResponse
import com.example.pulsemusic.feature.studio.data.model.UpdateExternalSongParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

// Репозиторій студії (наразі функціонал студії поєднано у бібліотеку)
interface StudioRepository {
    suspend fun createLocalSong(
        title: String,
        description: String? = null,
        originalAuthor: String? = null,
        categoryIds: List<String>? = null,
        songFile: File? = null,
        coverFile: File? = null,
    ): Result<StudioIdResponse>

    suspend fun updateLocalSong(
        id: String,
        title: String? = null,
        description: String? = null,
        originalAuthor: String? = null,
        categoryIds: List<String>? = null,
        songFile: File? = null,
        coverFile: File? = null,
    ): Result<Unit>

    suspend fun deleteLocalSong(id: String): Result<Unit>

    suspend fun createExternalSong(
        externalUrl: String,
        categoryIds: List<String>? = null,
    ): Result<StudioIdResponse>

    suspend fun createExternalSong(
        request: CreateExternalSong,
    ): Result<StudioIdResponse>

    suspend fun updateExternalSong(
        id: String,
        externalUrl: String? = null,
        categoryIds: List<String>? = null,
    ): Result<Unit>

    suspend fun updateExternalSong(
        id: String,
        request: UpdateExternalSongParams,
    ): Result<Unit>

    suspend fun deleteExternalSong(id: String): Result<Unit>
}

class StudioRepositoryImpl(
    private val studioApiService: StudioApiService,
    private val authPreferences: AuthPreferences,
) : StudioRepository {

    private fun tokenProvider(): String? {
        val token = authPreferences.accessToken.value
        return if (!token.isNullOrBlank()) "Bearer $token" else null
    }

    override suspend fun createLocalSong(
        title: String,
        description: String?,
        originalAuthor: String?,
        categoryIds: List<String>?,
        songFile: File?,
        coverFile: File?,
    ): Result<StudioIdResponse> = withContext(Dispatchers.IO) {
        val token = tokenProvider() ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val titlePart = MultipartBody.Part.createFormData("title", title)
            val descPart = description?.let { MultipartBody.Part.createFormData("description", it) }
            val authorPart = originalAuthor?.let { MultipartBody.Part.createFormData("originalAuthor", it) }
            val catPart = categoryIds?.joinToString(",")?.let { MultipartBody.Part.createFormData("categoryIds", it) }
            val songPart = songFile?.let { file ->
                val requestBody = file.asRequestBody("audio/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("song", file.name, requestBody)
            }
            val coverPart = coverFile?.let { file ->
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("cover", file.name, requestBody)
            }

            val response = studioApiService.createLocalSong(
                token = token,
                title = titlePart,
                description = descPart,
                originalAuthor = authorPart,
                categoryIds = catPart,
                song = songPart,
                cover = coverPart,
            )

            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Помилка створення локальної пісні: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLocalSong(
        id: String,
        title: String?,
        description: String?,
        originalAuthor: String?,
        categoryIds: List<String>?,
        songFile: File?,
        coverFile: File?,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val token = tokenProvider() ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val titlePart = title?.let { MultipartBody.Part.createFormData("title", it) }
            val descPart = description?.let { MultipartBody.Part.createFormData("description", it) }
            val authorPart = originalAuthor?.let { MultipartBody.Part.createFormData("originalAuthor", it) }
            val catPart = categoryIds?.joinToString(",")?.let { MultipartBody.Part.createFormData("categoryIds", it) }
            val songPart = songFile?.let { file ->
                val requestBody = file.asRequestBody("audio/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("song", file.name, requestBody)
            }
            val coverPart = coverFile?.let { file ->
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("cover", file.name, requestBody)
            }

            val response = studioApiService.updateLocalSong(
                token = token,
                id = id,
                title = titlePart,
                description = descPart,
                originalAuthor = authorPart,
                categoryIds = catPart,
                song = songPart,
                cover = coverPart,
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Помилка оновлення локальної пісні: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLocalSong(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = tokenProvider() ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val response = studioApiService.deleteLocalSong(token, id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Помилка видалення локальної пісні: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createExternalSong(
        externalUrl: String,
        categoryIds: List<String>?,
    ): Result<StudioIdResponse> {
        return createExternalSong(CreateExternalSong(externalUrl, categoryIds))
    }

    override suspend fun createExternalSong(request: CreateExternalSong): Result<StudioIdResponse> = withContext(Dispatchers.IO) {
        val token = tokenProvider() ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val response = studioApiService.createExternalSong(token, request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Помилка створення зовнішньої пісні: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExternalSong(
        id: String,
        externalUrl: String?,
        categoryIds: List<String>?,
    ): Result<Unit> {
        return updateExternalSong(id, UpdateExternalSongParams(externalUrl, categoryIds))
    }

    override suspend fun updateExternalSong(
        id: String,
        request: UpdateExternalSongParams,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val token = tokenProvider() ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val response = studioApiService.updateExternalSong(token, id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Помилка оновлення зовнішньої пісні: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExternalSong(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = tokenProvider() ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val response = studioApiService.deleteExternalSong(token, id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Помилка видалення зовнішньої пісні: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
