package com.example.pulsemusic.feature.library.data

import com.example.pulsemusic.core.data.model.SongRankingItem
import com.example.pulsemusic.core.data.model.CountResponse
import com.example.pulsemusic.feature.library.data.model.CreatePlaylist
import com.example.pulsemusic.feature.library.data.model.CreatePlaylistResponse
import com.example.pulsemusic.feature.library.data.model.PlaylistResponse
import com.example.pulsemusic.feature.library.data.model.SongHistoryItem
import com.example.pulsemusic.feature.library.data.model.UpdatePlaylist
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Сервіс бібліотеки
interface LibraryApiService {
    @POST("api/users/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") token: String,
        @Body request: CreatePlaylist
    ): Response<CreatePlaylistResponse>

    @GET("api/users/playlists/{id}")
    suspend fun getPlaylistDetails(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<PlaylistResponse>

    @DELETE("api/users/playlists/{id}")
    suspend fun deletePlaylist(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @PUT("api/users/playlists/{id}")
    suspend fun updatePlaylist(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdatePlaylist
    ): Response<Unit>

    @DELETE("api/users/playlists/{id}/songs/{songId}")
    suspend fun removeSongFromPlaylist(
        @Header("Authorization") token: String,
        @Path("id") playlistId: String,
        @Path("songId") songId: String
    ): Response<Unit>

    @GET("api/users/history/count")
    suspend fun getHistoryCount(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null
    ): Response<CountResponse>

    @GET("api/users/history")
    suspend fun getHistory(
        @Header("Authorization") token: String,
        @Query("from") from: Int,
        @Query("to") to: Int,
        @Query("search") search: String? = null
    ): Response<List<SongHistoryItem>>

    @GET("api/users/likes/count")
    suspend fun getLikesCount(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null
    ): Response<CountResponse>

    @GET("api/users/likes")
    suspend fun getLikes(
        @Header("Authorization") token: String,
        @Query("from") from: Int,
        @Query("to") to: Int,
        @Query("search") search: String? = null
    ): Response<List<SongRankingItem>>
}
