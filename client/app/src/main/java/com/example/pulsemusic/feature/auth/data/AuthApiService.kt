package com.example.pulsemusic.feature.auth.data

import com.example.pulsemusic.feature.auth.data.model.AuthCredentials
import com.example.pulsemusic.feature.auth.data.model.AuthResponse
import com.example.pulsemusic.feature.auth.data.model.LogoutRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

//  Сервіс автентифікації
interface AuthApiService {
    @POST("api/auth/register")
    suspend fun register(@Body credentials: AuthCredentials): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body credentials: AuthCredentials): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Unit>
}
