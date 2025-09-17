package com.example.feature.notes.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.notes.home.components.LanguageSelector
import com.example.feature.notes.home.components.RecentNotesSection
import com.example.feature.notes.home.components.StatusSection
import com.example.feature.notes.home.components.VoiceButton

@Composable
fun HomeScreen(onNavigateToNotesList: () -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.handleIntent(HomeContract.Intent.StartVoiceRecognition)
        } else {
            // Handle permission denied
            viewModel.handleIntent(HomeContract.Intent.ClearTranscription)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when(effect){
                HomeContract.Effect.NavigateToNotesList -> onNavigateToNotesList()
                HomeContract.Effect.RequestMicrophonePermission -> {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
                is HomeContract.Effect.ShowError -> Toast.makeText(context, effect.error, Toast.LENGTH_SHORT)
                is HomeContract.Effect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT)
            }
        }
    }

    HomeContent(
        state = state,
        onIntent = viewModel::handleIntent
    )
}

@Composable
private fun HomeContent(
    state: HomeContract.State,
    onIntent: (HomeContract.Intent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Voice Notes",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(vertical = 24.dp)
        )
        LanguageSelector(
            selectedLanguage = state.selectedLanguage,
            onLanguageSelected = { language ->
                onIntent(HomeContract.Intent.ChangeLanguage(language))
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        VoiceButton(
            voiceState = state.voiceState,
            volumeLevel = state.volumeLevel,
            onClick = {
                when (state.voiceState) {
                    HomeContract.VoiceState.Idle -> {
                        onIntent(HomeContract.Intent.StartVoiceRecognition)
                    }

                    else -> {
                        onIntent(HomeContract.Intent.StopVoiceRecognition)
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        StatusSection(
            state = state,
            onRetry = { onIntent(HomeContract.Intent.RetryVoiceRecognition) },
            onClear = { onIntent(HomeContract.Intent.ClearTranscription) }
        )
        Spacer(modifier = Modifier.height(32.dp))

        if(state.recentNotes.isNotEmpty()) {
            RecentNotesSection(
                notes = state.recentNotes,
                onViewAllClick = {
                    onIntent(HomeContract.Intent.NavigateToNotesList)
                }
            )
        }
        if(state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}