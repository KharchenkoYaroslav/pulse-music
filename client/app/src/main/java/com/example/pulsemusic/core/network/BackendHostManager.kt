package com.example.pulsemusic.core.network

import com.example.pulsemusic.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject
import javax.inject.Singleton

// Логіка побудови посилань
@Singleton
class BackendHostManager @Inject constructor() {
    private val candidateHosts = BuildConfig.BACKEND_HOSTS.split(",")
    var activeBackendHost: String = candidateHosts.firstOrNull() ?: "127.0.0.1"

    fun buildFullUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path

        val cleanPath = path.replace("\\", "/").removePrefix("/")
        val baseFileUrl = BuildConfig.API_FILE_URL.takeIf { it.isNotBlank() } ?: return null

        val baseHttpUrl = baseFileUrl.toHttpUrlOrNull()
        if (baseHttpUrl != null) {
            val targetBaseUrl = baseHttpUrl.newBuilder().host(activeBackendHost).build()
            val lastSegment = targetBaseUrl.pathSegments.lastOrNull { it.isNotEmpty() }

            val relativePath = if (lastSegment != null && cleanPath.lowercase().startsWith("${lastSegment.lowercase()}/")) {
                cleanPath.substring(lastSegment.length + 1)
            } else {
                cleanPath
            }

            return targetBaseUrl.resolve(relativePath)?.toString()
        }

        val replacedBase = baseFileUrl.replace(Regex("""(?<=://)[^:/]+"""), activeBackendHost)
        return if (replacedBase.endsWith("/")) {
            "$replacedBase$cleanPath"
        } else {
            "$replacedBase/$cleanPath"
        }
    }
}
