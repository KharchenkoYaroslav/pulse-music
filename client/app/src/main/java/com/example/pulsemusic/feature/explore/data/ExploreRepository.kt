package com.example.pulsemusic.feature.explore.data

import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.network.BackendHostManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ExploreRepository {
    suspend fun getCountByLikes(
        categoryId: String? = null,
        authorId: String? = null,
        search: String? = null,
    ): Result<Int>

    suspend fun getCountByViews(
        categoryId: String? = null,
        authorId: String? = null,
        search: String? = null,
    ): Result<Int>

    suspend fun getCountByNewest(
        categoryId: String? = null,
        authorId: String? = null,
        search: String? = null,
    ): Result<Int>

    suspend fun getTrendingByViewsDirectly(
        from: Int,
        to: Int,
        categoryId: String? = null,
        authorId: String? = null,
        search: String? = null,
    ): Result<List<SongRankingItem>>

    suspend fun getTrendingByLikesDirectly(
        from: Int,
        to: Int,
        categoryId: String? = null,
        authorId: String? = null,
        search: String? = null,
    ): Result<List<SongRankingItem>>

    suspend fun getTrendingByNewestDirectly(
        from: Int,
        to: Int,
        categoryId: String? = null,
        authorId: String? = null,
        search: String? = null,
    ): Result<List<SongRankingItem>>
}

// Репозиторій Головного екрану
class ExploreRepositoryImpl(
    private val exploreApiService: ExploreApiService,
    private val backendHostManager: BackendHostManager
) : ExploreRepository {

    override suspend fun getCountByLikes(
        categoryId: String?,
        authorId: String?,
        search: String?,
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = exploreApiService.getCountByLikes(
                categoryId = categoryId,
                authorId = authorId,
                search = search,
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.count ?: 0)
            } else {
                Result.failure(Exception("Помилка отримання кількості за вподобаннями: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCountByViews(
        categoryId: String?,
        authorId: String?,
        search: String?,
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = exploreApiService.getCountByViews(
                categoryId = categoryId,
                authorId = authorId,
                search = search,
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.count ?: 0)
            } else {
                Result.failure(Exception("Помилка отримання кількості за переглядами: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCountByNewest(
        categoryId: String?,
        authorId: String?,
        search: String?,
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = exploreApiService.getCountByNewest(
                categoryId = categoryId,
                authorId = authorId,
                search = search,
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.count ?: 0)
            } else {
                Result.failure(Exception("Помилка отримання кількості за новизною: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrendingByViewsDirectly(
        from: Int,
        to: Int,
        categoryId: String?,
        authorId: String?,
        search: String?
    ): Result<List<SongRankingItem>> = withContext(Dispatchers.IO) {
        try {
            val response = exploreApiService.getTrendingByViews(
                from = from,
                to = to,
                categoryId = categoryId,
                authorId = authorId,
                search = search,
            )
            if (response.isSuccessful) {
                val mapped = response.body()?.map { item ->
                    item.copy(cover = backendHostManager.buildFullUrl(item.cover))
                } ?: emptyList()
                Result.success(mapped)
            } else {
                Result.failure(Exception("Помилка отримання: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrendingByLikesDirectly(
        from: Int,
        to: Int,
        categoryId: String?,
        authorId: String?,
        search: String?
    ): Result<List<SongRankingItem>> = withContext(Dispatchers.IO) {
        try {
            val response = exploreApiService.getTrendingByLikes(
                from = from,
                to = to,
                categoryId = categoryId,
                authorId = authorId,
                search = search,
            )
            if (response.isSuccessful) {
                val mapped = response.body()?.map { item ->
                    item.copy(cover = backendHostManager.buildFullUrl(item.cover))
                } ?: emptyList()
                Result.success(mapped)
            } else {
                Result.failure(Exception("Помилка отримання: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrendingByNewestDirectly(
        from: Int,
        to: Int,
        categoryId: String?,
        authorId: String?,
        search: String?
    ): Result<List<SongRankingItem>> = withContext(Dispatchers.IO) {
        try {
            val response = exploreApiService.getTrendingByNewest(
                from = from,
                to = to,
                categoryId = categoryId,
                authorId = authorId,
                search = search,
            )
            if (response.isSuccessful) {
                val mapped = response.body()?.map { item ->
                    item.copy(cover = backendHostManager.buildFullUrl(item.cover))
                } ?: emptyList()
                Result.success(mapped)
            } else {
                Result.failure(Exception("Помилка отримання: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
