package com.example.pulsemusic.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.LinkedHashMap
import java.util.Locale
import java.util.TimeZone

// Форматори даних
@Composable
fun rememberTimestampFormatter(pattern: String = "dd MMM, HH:mm"): (Long) -> String {
    return remember(pattern) {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        val lambda: (Long) -> String = { timestamp -> formatter.format(Date(timestamp)) }
        lambda
    }
}

fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}

fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> {
            val formatted = String.format(Locale.US, "%.1f", count / 1_000_000.0)
            "${formatted.removeSuffix(".0")}M"
        }
        count >= 1_000 -> {
            val formatted = String.format(Locale.US, "%.1f", count / 1_000.0)
            "${formatted.removeSuffix(".0")}K"
        }
        else -> count.toString()
    }
}

fun formatIsoDate(dateStr: String?, outputPattern: String = "dd.MM.yyyy"): String {
    if (dateStr.isNullOrBlank()) return "Завантаження..."
    val outputFormatter = SimpleDateFormat(outputPattern, Locale.getDefault())
    val patterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (pattern in patterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsedDate = parser.parse(dateStr)
            if (parsedDate != null) {
                return outputFormatter.format(parsedDate)
            }
        } catch (_: Exception) {
            // continue
        }
    }
    return dateStr
}

fun <T> groupItemsByTimeCategory(
    items: List<T>,
    timestampSelector: (T) -> Long
): Map<String, List<T>> {
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val yesterdayStart = todayStart - 86400000L
    val thisWeekStart = todayStart - 6 * 86400000L
    val thisMonthStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val map = LinkedHashMap<String, MutableList<T>>()

    for (item in items) {
        val time = timestampSelector(item)
        val category = when {
            time >= todayStart -> "Сьогодні"
            time >= yesterdayStart -> "Вчора"
            time >= thisWeekStart -> "Цього тижня"
            time >= thisMonthStart -> "Цього місяця"
            else -> "Раніше"
        }
        map.getOrPut(category) { mutableListOf() }.add(item)
    }

    return map
}

