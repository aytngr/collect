package com.example.feature.notes.home

import androidx.lifecycle.viewModelScope
import com.example.core.common.base.BaseViewModel
import com.example.core.common.base.permissions.PermissionHandler
import com.example.domain.models.Language
import com.example.domain.models.VoiceRecognitionResult
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.repository.VoiceRecognitionRepository
import com.example.domain.usecase.CreateNoteUseCase
import com.example.domain.usecase.GetNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val createNoteUseCase: CreateNoteUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val permissionHandler: PermissionHandler,
    private val voiceRecognitionRepository: VoiceRecognitionRepository
) : BaseViewModel<HomeContract.Intent, HomeContract.State, HomeContract.Effect>(HomeContract.State()){

    private var voiceRecognitionJob: Job? = null

    init {
        loadRecentNotes()
    }

    override fun handleIntent(intent: HomeContract.Intent) {
        when(intent){
            is HomeContract.Intent.StartVoiceRecognition -> startVoiceRecognition()
            is HomeContract.Intent.StopVoiceRecognition -> stopVoiceRecognition()
            is HomeContract.Intent.ChangeLanguage -> changeLanguage(intent.language)
            is HomeContract.Intent.ProcessVoiceResult -> processVoiceResult(intent.text)
            is HomeContract.Intent.RetryVoiceRecognition -> retryVoiceRecognition()
            is HomeContract.Intent.ClearTranscription -> clearTranscription()
            is HomeContract.Intent.NavigateToNotesList -> navigateToNotesList()
        }
    }

    private fun startVoiceRecognition(){
        if (!permissionHandler.hasMicrophonePermission()) {
            sendEffect(HomeContract.Effect.RequestMicrophonePermission)
            return
        }
        setState {
            copy(
                voiceState = HomeContract.VoiceState.WaitingForCommand,
                transcribedText = "",
                error = null
            )
        }

        voiceRecognitionJob = voiceRecognitionRepository.startListening(currentState.selectedLanguage)
            .onEach { result ->
                when(result) {
                    VoiceRecognitionResult.Listening -> {
                        setState { copy(voiceState = HomeContract.VoiceState.WaitingForCommand) }
                    }
                    is VoiceRecognitionResult.Success -> {
                        handleVoiceResult(result.text)
                    }
                    is VoiceRecognitionResult.Error -> {
                        setState {
                            copy(
                                voiceState = HomeContract.VoiceState.Idle,
                                error = result.message
                            )
                        }
                        sendEffect(HomeContract.Effect.ShowError(result.message))
                    }
                    is VoiceRecognitionResult.Idle -> {
                        setState { copy(voiceState = HomeContract.VoiceState.Idle) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleVoiceResult(text: String) {
        when (currentState.voiceState) {
            HomeContract.VoiceState.WaitingForCommand -> {
                if (isCreateNoteCommand(text)) {
                    setState {
                        copy(
                            voiceState = HomeContract.VoiceState.ListeningForContent,
                            transcribedText = getListeningPrompt()
                        )
                    }
                    // Continue listening for note content
                    viewModelScope.launch {
                        delay(1000)
                        startListeningForContent()
                    }
                } else {
                    setState {
                        copy(
                            voiceState = HomeContract.VoiceState.Idle,
                            transcribedText = getCommandNotRecognizedMessage(),
                            error = "Command not recognized"
                        )
                    }
                }
            }
            HomeContract.VoiceState.ListeningForContent -> {
                processNoteContent(text)
            }
            else -> {}
        }
    }

    private fun isCreateNoteCommand(text: String): Boolean {
        val lowercaseText = text.lowercase()
        return when (currentState.selectedLanguage) {
            Language.TURKISH -> lowercaseText.contains("not oluştur") ||
                    lowercaseText.contains("not yarat") ||
                    lowercaseText.contains("note oluştur")
            Language.AZERBAIJANI -> lowercaseText.contains("not yarat") ||
                    lowercaseText.contains("qeyd yarat")
            Language.ENGLISH -> lowercaseText.contains("create note") ||
                    lowercaseText.contains("make note")
        }
    }

    private fun getListeningPrompt(): String {
        return when (currentState.selectedLanguage) {
            Language.TURKISH -> "Komut tanındı. Şimdi notunuzu söyleyin..."
            Language.AZERBAIJANI -> "Komanda tanındı. İndi qeydinizi deyin..."
            Language.ENGLISH -> "Command recognized. Now speak your note..."
        }
    }

    private fun getCommandNotRecognizedMessage(): String {
        return when (currentState.selectedLanguage) {
            Language.TURKISH -> "'Not oluştur' deyin"
            Language.AZERBAIJANI -> "'Not yarat' deyin"
            Language.ENGLISH -> "Say 'Create Note'"
        }
    }

    private fun startListeningForContent() {
        voiceRecognitionJob?.cancel()
        voiceRecognitionJob = voiceRecognitionRepository
            .startListening(currentState.selectedLanguage)
            .onEach { result ->
                when (result) {
                    is VoiceRecognitionResult.Success -> {
                        processNoteContent(result.text)
                    }
                    is VoiceRecognitionResult.Error -> {
                        setState {
                            copy(
                                voiceState = HomeContract.VoiceState.Idle,
                                error = result.message
                            )
                        }
                    }
                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    private fun processNoteContent(content: String) {
        setState {
            copy(
                voiceState = HomeContract.VoiceState.Processing,
                transcribedText = content
            )
        }

        viewModelScope.launch {
            createNoteUseCase(content, null, currentState.selectedLanguage)
                .onSuccess { note ->
                    setState {
                        copy(
                            voiceState = HomeContract.VoiceState.Idle,
                            transcribedText = getSuccessMessage()
                        )
                    }
                    sendEffect(HomeContract.Effect.ShowToast("Note created!"))
                    loadRecentNotes()

                    // Clear transcription after delay
                    delay(2000)
                    setState { copy(transcribedText = "") }
                }
                .onError { error ->
                    val errorMessage = error.message ?: "Failed to create note"
                    setState {
                        copy(
                            voiceState = HomeContract.VoiceState.Idle,
                            error = errorMessage
                        )
                    }
                    sendEffect(HomeContract.Effect.ShowError(errorMessage))
                }
        }
    }

    private fun getSuccessMessage(): String {
        return when (currentState.selectedLanguage) {
            Language.TURKISH -> "Not başarıyla oluşturuldu!"
            Language.AZERBAIJANI -> "Not uğurla yaradıldı!"
            Language.ENGLISH -> "Note created successfully!"
        }
    }

    private fun stopVoiceRecognition() {
        voiceRecognitionJob?.cancel()
        voiceRecognitionRepository.stopListening()
        setState { copy(voiceState = HomeContract.VoiceState.Idle) }
    }

    private fun changeLanguage(language: Language) {
        setState { copy(selectedLanguage = language) }
        if (currentState.voiceState != HomeContract.VoiceState.Idle) {
            // Restart recognition with new language
            stopVoiceRecognition()
            startVoiceRecognition()
        }
    }

    private fun processVoiceResult(text: String) {
        handleVoiceResult(text)
    }

    private fun retryVoiceRecognition() {
        setState { copy(error = null) }
        startVoiceRecognition()
    }

    private fun clearTranscription() {
        setState { copy(transcribedText = "", error = null) }
    }

    private fun navigateToNotesList() {
        sendEffect(HomeContract.Effect.NavigateToNotesList)
    }

    private fun loadRecentNotes() {
        getNotesUseCase.invoke()
            .onEach { notes ->
                notes.onSuccess {
                    setState { copy(recentNotes = it.take(3)) }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecognitionJob?.cancel()
        voiceRecognitionRepository.stopListening()
    }

}