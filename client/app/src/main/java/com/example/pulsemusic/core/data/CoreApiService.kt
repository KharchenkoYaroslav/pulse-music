package com.example.pulsemusic.core.data

import com.example.pulsemusic.core.data.model.CategoryListResponse
import com.example.pulsemusic.core.data.model.ToggleLikeResponse
import com.example.pulsemusic.core.data.model.UserPlaylist
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Загально використовувані запити
interface CoreApiService {
    @GET("api/categories")
    suspend fun getCategories(): Response<List<CategoryListResponse>>

    @GET("api/users/playlists")
    suspend fun getUserPlaylists(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null
    ): Response<List<UserPlaylist>>

    @POST("api/song/{id}/toggle-like")
    suspend fun toggleLike(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ToggleLikeResponse>
}
