package com.example.pulsemusic.feature.player.data

import com.example.pulsemusic.feature.player.data.model.CheckSongInPlaylistsRequest
import com.example.pulsemusic.feature.player.data.model.RecordViewResponse
import com.example.pulsemusic.feature.player.data.model.Song

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// Сервіс плеєру
interface PlayerApiService {
    @GET("api/song/{id}")
    suspend fun getSongDetails(
        @Header("Authorization") token: String?,
        @Path("id") id: String
    ): Response<Song>

    @POST("api/song/{id}/view")
    suspend fun recordView(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<RecordViewResponse>

    @POST("api/users/playlists/check-song/{songId}")
    suspend fun checkSongInPlaylists(
        @Header("Authorization") token: String,
        @Path("songId") songId: String,
        @Body body: CheckSongInPlaylistsRequest
    ): Response<Map<String, Boolean>>

    @POST("api/users/playlists/{id}/songs/{songId}")
    suspend fun addSongToPlaylist(
        @Header("Authorization") token: String,
        @Path("id") playlistId: String,
        @Path("songId") songId: String
    ): Response<Unit>

    @DELETE("api/users/playlists/{id}/songs/{songId}")
    suspend fun removeSongFromPlaylist(
        @Header("Authorization") token: String,
        @Path("id") playlistId: String,
        @Path("songId") songId: String
    ): Response<Unit>
}