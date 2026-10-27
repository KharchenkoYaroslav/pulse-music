@file:Suppress("unused")

package com.example.pulsemusic.di

import com.example.pulsemusic.BuildConfig
import com.example.pulsemusic.core.data.AuthPreferences
import com.example.pulsemusic.core.network.BackendHostManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// Мережевий провайдер
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authPreferences: AuthPreferences,
        backendHostManager: BackendHostManager
    ): OkHttpClient {
        val candidateHosts = BuildConfig.BACKEND_HOSTS.split(",")
        return OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url

                val hostsToTry = mutableListOf<String>()
                val currentActive = backendHostManager.activeBackendHost
                if (!hostsToTry.contains(currentActive)) {
                    hostsToTry.add(currentActive)
                }

                candidateHosts.forEach { h ->
                    if (!hostsToTry.contains(h)) hostsToTry.add(h)
                }

                var lastException: IOException? = null
                for (host in hostsToTry) {
                    try {
                        val newUrl = url.newBuilder().host(host).build()
                        val newRequest = request.newBuilder().url(newUrl).build()
                        val response = chain.proceed(newRequest)
                        backendHostManager.activeBackendHost = host
                        return@addInterceptor response
                    } catch (e: IOException) {
                        lastException = e
                    }
                }
                throw lastException ?: IOException("Не вдалося підключитися до сервера")
            }
            .authenticator { _, response ->
                if (responseCount(response) >= 2) return@authenticator null

                val currentRefreshToken = authPreferences.provideRefreshToken()
                if (currentRefreshToken.isNullOrBlank()) return@authenticator null

                val path = response.request.url.encodedPath
                if (path.contains("/api/auth/login") || path.contains("/api/auth/register") || path.contains("/api/auth/refresh")) {
                    return@authenticator null
                }

                try {
                    val host = backendHostManager.activeBackendHost
                    val baseUri = try {
                        URI(BuildConfig.API_BASE_URL)
                    } catch (_: Exception) { null }
                    val portPart = if (baseUri != null && baseUri.port != -1) ":${baseUri.port}" else ":3000"
                    val scheme = baseUri?.scheme ?: "http"
                    val refreshUrl = "$scheme://$host$portPart/api/auth/refresh"
                    val refreshReq = Request.Builder()
                        .url(refreshUrl)
                        .post(ByteArray(0).toRequestBody(null, 0, 0))
                        .addHeader("Authorization", "Bearer $currentRefreshToken")
                        .build()

                    val refreshResp = OkHttpClient().newCall(refreshReq).execute()
                    if (refreshResp.isSuccessful) {
                        val bodyStr = refreshResp.body?.string()
                        if (!bodyStr.isNullOrBlank()) {
                            val json = JSONObject(bodyStr)
                            val newAccess = json.optString("accessToken")
                            val newRefresh = json.optString("refreshToken")
                            if (newAccess.isNotBlank()) {
                                authPreferences.onTokensRefreshed(newAccess, newRefresh)
                                return@authenticator response.request.newBuilder()
                                    .header("Authorization", "Bearer $newAccess")
                                    .build()
                            }
                        }
                    } else if (refreshResp.code == 401 || refreshResp.code == 403) {
                        authPreferences.onSessionExpired()
                    }
                } catch (_: Exception) {}

                null
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
