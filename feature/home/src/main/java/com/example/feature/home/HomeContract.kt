package com.example.feature.home

import com.example.core.common.base.BaseEffect
import com.example.core.common.base.BaseIntent
import com.example.core.common.base.BaseState
import com.example.domain.models.Language
import com.example.domain.models.Note

class HomeContract {
    sealed class Intent : BaseIntent {
        object StartVoiceRecognition : Intent()
        object StopVoiceRecognition : Intent()
        data class ChangeLanguage(val language: Language) : Intent()
        data class ProcessVoiceResult(val text: String) : Intent()
        object RetryVoiceRecognition : Intent()
        object ClearTranscription : Intent()
        object CreateNewNote : Intent()
    }

    data class State(
        val voiceState: VoiceState = VoiceState.Idle,
        val selectedLanguage: Language = Language.TURKISH,
        val transcribedText: String = "",
        val recentNotes: List<Note> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val volumeLevel: Float = 0f,
        val date: String = "",
        val greeting: String = "",
        val reminderCount: Int = 0
    ) : BaseState

    sealed class Effect : BaseEffect {
        data class ShowToast(val message: String) : Effect()
        data class NavigateToNoteDetail(val id: Long) : Effect()
        data class ShowError(val error: String) : Effect()
        object RequestMicrophonePermission : Effect()
    }

    enum class VoiceState {
        Idle,               // Not recording
        WaitingForCommand,  // Listening for "create note"
        ListeningForContent,// Listening for actual note content
        Processing          // Creating the note
    }
}