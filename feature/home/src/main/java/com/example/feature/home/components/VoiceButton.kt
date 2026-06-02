package com.example.feature.home.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.feature.home.HomeContract

@Composable
fun VoiceButton(
    voiceState: HomeContract.VoiceState,
    volumeLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = voiceState != HomeContract.VoiceState.Idle

    val animatedScale by animateFloatAsState(
        targetValue = if (isListening) 1f + (volumeLevel / 100f) * 0.3f else 1f,
        animationSpec = tween(100),
        label = "voice_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val animatedRadii = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 40f,
            targetValue = 70f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, delayMillis = index * 200),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse_ring_$index"
        )
    }
    val animatedAlphas = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, delayMillis = index * 200),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse_alpha_$index"
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(140.dp)
    ) {
        if (isListening && voiceState != HomeContract.VoiceState.Processing) {
            val primaryColor = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.fillMaxSize()) {
                animatedRadii.forEachIndexed { index, radius ->
                    drawCircle(
                        color = primaryColor.copy(alpha = animatedAlphas[index].value),
                        radius = radius.value,
                        center = this.center // Use DrawScope's center
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(80.dp).scale(animatedScale),
            shape = CircleShape,
            containerColor = when (voiceState) {
                HomeContract.VoiceState.Idle -> MaterialTheme.colorScheme.primary
                HomeContract.VoiceState.Processing -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.error
            }
        ) {
            when (voiceState) {
                HomeContract.VoiceState.Processing -> {
                    Box(
                        modifier = Modifier.wrapContentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 3.dp
                        )
                    }
                }
                HomeContract.VoiceState.Idle -> {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Recording",
                        modifier = Modifier.size(32.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Stop Recording",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}