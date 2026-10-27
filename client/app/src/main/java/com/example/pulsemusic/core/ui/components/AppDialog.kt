package com.example.pulsemusic.core.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// базовий компонент діалогу
@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    dismissButtonText: String = "Скасувати",
    confirmButtonColor: Color = Color.White,
    confirmButtonEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF282828))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(spacing = MarqueeSpacing(16.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                content()
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(dismissButtonText, color = Color.Gray)
                    }
                    TextButton(
                        onClick = onConfirm,
                        enabled = confirmButtonEnabled
                    ) {
                        Text(
                            confirmButtonText,
                            color = if (confirmButtonEnabled) confirmButtonColor else confirmButtonColor.copy(alpha = 0.38f)
                        )
                    }
                }
            }
        }
    }
}
