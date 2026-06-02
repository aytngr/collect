package com.example.feature.overlay

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun ScreenshotFlashAnimation(){
    var alpha by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 100)
        ){ value, _ ->
            alpha = value
        }

        delay(50)

        animate(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = tween(durationMillis = 100)
        ){ value, _ ->
            alpha = value
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = alpha))
    )
}