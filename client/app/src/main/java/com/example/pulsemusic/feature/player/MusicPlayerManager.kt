package com.example.pulsemusic.feature.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.net.toUri

@OptIn(UnstableApi::class)
class MusicPlayerManager(context: Context) {
    // Налаштування джерел даних
    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(15000)

    private val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

    private val mediaSourceFactory = DefaultMediaSourceFactory(context)
        .setDataSourceFactory(dataSourceFactory)

    // Налаштування аудіо-атрибутів
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()
    // Ініціалізація плеєра
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .setAudioAttributes(audioAttributes, true)
        .build()

    // Область корутин та завдання
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    // Чи пісня відтворюється
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    // Таймінг відтворення
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()
    // Тривалість треку
    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()
    // Чи відбувається завантаження/буферизація аудіопотоку
    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()

    // Колбек завершення відтворення пісні
    var onSongEnded: (() -> Unit)? = null

    // Логіка реагування на події плеєра
    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                _isPlaying.value = playWhenReady
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                if (playing) startProgressTracker() else stopProgressTracker()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _isBuffering.value = (playbackState == Player.STATE_BUFFERING)

                if (playbackState == Player.STATE_READY) {
                    _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    onSongEnded?.invoke()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("MusicPlayerManager", "ExoPlayer error: ${error.message}", error)
                _isBuffering.value = false
                _isPlaying.value = false
            }
        })
    }
    fun play(url: String) {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _currentPosition.value = 0L
        _duration.value = 0L

        val uri = if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("content://") || url.startsWith("file://")) {
            url.toUri()
        } else {
            Uri.fromFile(File(url))
        }

        val mediaItemBuilder = MediaItem.Builder().setUri(uri)
        val path = uri.path ?: url
        if (path.endsWith(".mp3", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(MimeTypes.AUDIO_MPEG)
        }

        exoPlayer.setMediaItem(mediaItemBuilder.build())
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayPause() {
        val isNowPlaying = _isPlaying.value
        if (isNowPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _isPlaying.value = false
        exoPlayer.pause()
    }

    fun resume() {
        _isPlaying.value = true
        if (exoPlayer.playbackState == Player.STATE_IDLE) {
            exoPlayer.prepare()
        }
        exoPlayer.play()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                _currentPosition.value = exoPlayer.currentPosition
                _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                delay(500.milliseconds)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
    }

    fun release() {
        stopProgressTracker()
        scope.cancel()
        exoPlayer.release()
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
        stopProgressTracker()
    }
}
