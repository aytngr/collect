package com.example.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.models.Language
import com.example.feature.home.HomeContract

@Composable
fun StatusSection(
    state: HomeContract.State,
    onRetry: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Text
        Text(
            text = getStatusText(state.voiceState, state.selectedLanguage),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Transcribed Text Card
        AnimatedVisibility(
            visible = state.transcribedText.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = state.transcribedText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Action buttons
                    if (state.voiceState == HomeContract.VoiceState.Idle && state.transcribedText.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = onClear) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear text"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Error Message
        AnimatedVisibility(
            visible = state.error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onRetry) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

private fun getStatusText(voiceState: HomeContract.VoiceState, language: Language): String {
    return when (voiceState) {
        HomeContract.VoiceState.Idle -> when (language) {
            Language.TURKISH -> "Başlamak için mikrofona dokunun"
            Language.AZERBAIJANI -> "Başlamaq üçün mikrofona toxunun"
            Language.ENGLISH -> "Tap the microphone to start"
        }
        HomeContract.VoiceState.WaitingForCommand -> when (language) {
            Language.TURKISH -> "'Not oluştur' deyin..."
            Language.AZERBAIJANI -> "'Not yarat' deyin..."
            Language.ENGLISH -> "Say 'Create Note'..."
        }
        HomeContract.VoiceState.ListeningForContent -> when (language) {
            Language.TURKISH -> "Not içeriği için dinliyorum..."
            Language.AZERBAIJANI -> "Not məzmunu üçün dinləyirəm..."
            Language.ENGLISH -> "Listening for note content..."
        }
        HomeContract.VoiceState.Processing -> when (language) {
            Language.TURKISH -> "Notunuz oluşturuluyor..."
            Language.AZERBAIJANI -> "Qeydiniz yaradılır..."
            Language.ENGLISH -> "Creating your note..."
        }
    }
}