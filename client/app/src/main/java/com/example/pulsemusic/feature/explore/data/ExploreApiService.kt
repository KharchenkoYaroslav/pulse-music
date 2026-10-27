package com.example.pulsemusic.feature.explore.data

import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.data.model.CountResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Сервіс Головного екрану
interface ExploreApiService {
    @GET("api/recommendations/count/likes")
    suspend fun getCountByLikes(
        @Query("categoryId") categoryId: String? = null,
        @Query("authorId") authorId: String? = null,
        @Query("search") search: String? = null
    ): Response<CountResponse>

    @GET("api/recommendations/count/views")
    suspend fun getCountByViews(
        @Query("categoryId") categoryId: String? = null,
        @Query("authorId") authorId: String? = null,
        @Query("search") search: String? = null
    ): Response<CountResponse>

    @GET("api/recommendations/count/newest")
    suspend fun getCountByNewest(
        @Query("categoryId") categoryId: String? = null,
        @Query("authorId") authorId: String? = null,
        @Query("search") search: String? = null
    ): Response<CountResponse>

    @GET("api/recommendations/bylikes")
    suspend fun getTrendingByLikes(
        @Query("from") from: Int,
        @Query("to") to: Int,
        @Query("categoryId") categoryId: String? = null,
        @Query("authorId") authorId: String? = null,
        @Query("search") search: String? = null
    ): Response<List<SongRankingItem>>

    @GET("api/recommendations/byviews")
    suspend fun getTrendingByViews(
        @Query("from") from: Int,
        @Query("to") to: Int,
        @Query("categoryId") categoryId: String? = null,
        @Query("authorId") authorId: String? = null,
        @Query("search") search: String? = null
    ): Response<List<SongRankingItem>>

    @GET("api/recommendations/bynewest")
    suspend fun getTrendingByNewest(
        @Query("from") from: Int,
        @Query("to") to: Int,
        @Query("categoryId") categoryId: String? = null,
        @Query("authorId") authorId: String? = null,
        @Query("search") search: String? = null
    ): Response<List<SongRankingItem>>
}
