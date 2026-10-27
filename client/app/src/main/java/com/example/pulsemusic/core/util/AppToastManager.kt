package com.example.pulsemusic.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Менеджер повідомлень
object AppToastManager {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()

    fun showMessage(message: String) {
        _messages.tryEmit(message)
    }
}
