package com.example.pulsemusic.feature.player.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlin.math.abs

// Анімований прогрес-бар
@Composable
fun rememberAnimatedProgress(
    currentPositionMs: Long,
    durationMs: Long,
    isDraggingSlider: Boolean = false
): Animatable<Float, AnimationVector1D> {
    val progress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatableProgress = remember { Animatable(progress) }

    LaunchedEffect(progress) {
        if (isDraggingSlider) return@LaunchedEffect

        val diff = abs(animatableProgress.value - progress)
        val threshold = if (durationMs > 0) 2000f / durationMs.toFloat() else 0.02f
        if (diff > threshold) {
            animatableProgress.snapTo(progress)
        } else {
            animatableProgress.animateTo(
                targetValue = progress,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
        }
    }

    return animatableProgress
}
