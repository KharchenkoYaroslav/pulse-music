package com.example.pulsemusic.core.util

import android.util.Base64
import org.json.JSONObject

// Отримання користувача з токена
fun getUserIdFromToken(token: String?): String? {
    if (token.isNullOrBlank()) return null
    try {
        val parts = token.split(".")
        if (parts.size < 2) return null
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
        val json = JSONObject(payload)
        return json.optString("sub").takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
