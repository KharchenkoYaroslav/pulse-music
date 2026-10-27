package com.example.pulsemusic.feature.studio.data

import com.example.pulsemusic.feature.studio.data.model.CreateExternalSong
import com.example.pulsemusic.feature.studio.data.model.UpdateExternalSongParams
import com.example.pulsemusic.feature.studio.data.model.StudioIdResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// API сервіс студії (наразі функціонал студії поєднано у бібліотеку)
interface StudioApiService {
    @Multipart
    @POST("api/local-songs")
    suspend fun createLocalSong(
        @Header("Authorization") token: String,
        @Part title: MultipartBody.Part,
        @Part description: MultipartBody.Part?,
        @Part originalAuthor: MultipartBody.Part?,
        @Part categoryIds: MultipartBody.Part?,
        @Part song: MultipartBody.Part?,
        @Part cover: MultipartBody.Part?
    ): Response<StudioIdResponse>

    @Multipart
    @PATCH("api/local-songs/{id}")
    suspend fun updateLocalSong(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part title: MultipartBody.Part?,
        @Part description: MultipartBody.Part?,
        @Part originalAuthor: MultipartBody.Part?,
        @Part categoryIds: MultipartBody.Part?,
        @Part song: MultipartBody.Part?,
        @Part cover: MultipartBody.Part?
    ): Response<Unit>

    @DELETE("api/local-songs/{id}")
    suspend fun deleteLocalSong(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @POST("api/external-songs")
    suspend fun createExternalSong(
        @Header("Authorization") token: String,
        @Body request: CreateExternalSong
    ): Response<StudioIdResponse>

    @PATCH("api/external-songs/{id}")
    suspend fun updateExternalSong(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateExternalSongParams
    ): Response<Unit>

    @DELETE("api/external-songs/{id}")
    suspend fun deleteExternalSong(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
