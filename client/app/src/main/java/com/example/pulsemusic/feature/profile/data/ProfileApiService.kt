package com.example.pulsemusic.feature.profile.data

import com.example.pulsemusic.feature.profile.data.model.UpdateUser
import com.example.pulsemusic.feature.profile.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

// Сервіс профілю
interface ProfileApiService {
    @GET("api/users/profile/{id}")
    suspend fun getProfile(
        @Path("id") id: String
    ): Response<UserResponse>

    @PUT("api/users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateUser
    ): Response<UserResponse>

    @DELETE("api/users/profile")
    suspend fun deleteProfile(
        @Header("Authorization") token: String
    ): Response<UserResponse>
}
