package com.example.pulsemusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.example.pulsemusic.core.ui.MainAppScaffold
import com.example.pulsemusic.core.ui.theme.PulseMusicTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseMusicTheme {
                MainAppScaffold()
            }
        }
    }
}
