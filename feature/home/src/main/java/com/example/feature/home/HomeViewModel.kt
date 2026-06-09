package com.example.feature.home

import androidx.lifecycle.viewModelScope
import com.example.core.common.base.BaseViewModel
import com.example.core.common.base.permissions.PermissionHandler
import com.example.domain.models.Language
import com.example.domain.models.VoiceRecognitionResult
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.repository.VoiceRecognitionRepository
import com.example.domain.usecase.CreateEmptyNoteUseCase
import com.example.domain.usecase.GetNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import java.time.Duration

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val createEmptyNoteUseCase: CreateEmptyNoteUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val permissionHandler: PermissionHandler,
    private val voiceRecognitionRepository: VoiceRecognitionRepository
) : BaseViewModel<HomeContract.Intent, HomeContract.State, HomeContract.Effect>(HomeContract.State()){

    private var voiceRecognitionJob: Job? = null

    val formatter = DateTimeFormatter.ofPattern("EEE · MMM d", Locale.getDefault())

    init {
        setState {
            HomeContract.State(date = LocalDate.now().format(formatter), greeting = getGreeting())
        }
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
            is HomeContract.Intent.CreateNewNote -> createNewNote()
        }
    }

    private fun createNewNote(){
        viewModelScope.launch {
            createEmptyNoteUseCase()
                .onSuccess { note ->
                    sendEffect(HomeContract.Effect.NavigateToNoteDetail(note.id))
                }
                .onError { error ->
                    sendEffect(HomeContract.Effect.ShowError(error.message ?: ""))
                }
        }
    }

    private fun startVoiceRecognition(){
        if (!permissionHandler.hasMicrophonePermission()) {
            sendEffect(HomeContract.Effect.RequestMicrophonePermission)
            return
        }
        setState {
            HomeContract.State(
                voiceState = HomeContract.VoiceState.WaitingForCommand,
                transcribedText = "",
                error = null
            )
        }

        voiceRecognitionJob = voiceRecognitionRepository.startListening(currentState.selectedLanguage)
            .onEach { result ->
                when(result) {
                    VoiceRecognitionResult.Listening -> {
                        setState { HomeContract.State(voiceState = HomeContract.VoiceState.WaitingForCommand) }
                    }
                    is VoiceRecognitionResult.Success -> {
                        handleVoiceResult(result.text)
                    }
                    is VoiceRecognitionResult.Error -> {
                        setState {
                            HomeContract.State(
                                voiceState = HomeContract.VoiceState.Idle,
                                error = result.message
                            )
                        }
                        sendEffect(HomeContract.Effect.ShowError(result.message))
                    }
                    is VoiceRecognitionResult.Idle -> {
                        setState { HomeContract.State(voiceState = HomeContract.VoiceState.Idle) }
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
                        HomeContract.State(
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
                        HomeContract.State(
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
                            HomeContract.State(
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
            HomeContract.State(
                voiceState = HomeContract.VoiceState.Processing,
                transcribedText = content
            )
        }

//        viewModelScope.launch {
//            createNoteUseCase(null, content, null, currentState.selectedLanguage)
//                .onSuccess { note ->
//                    setState {
//                        HomeContract.State(
//                            voiceState = HomeContract.VoiceState.Idle,
//                            transcribedText = getSuccessMessage()
//                        )
//                    }
//                    sendEffect(HomeContract.Effect.ShowToast("Note created!"))
//                    loadRecentNotes()
//
//                    // Clear transcription after delay
//                    delay(2000)
//                    setState { HomeContract.State(transcribedText = "") }
//                }
//                .onError { error ->
//                    val errorMessage = error.message ?: "Failed to create note"
//                    setState {
//                        HomeContract.State(
//                            voiceState = HomeContract.VoiceState.Idle,
//                            error = errorMessage
//                        )
//                    }
//                    sendEffect(HomeContract.Effect.ShowError(errorMessage))
//                }
//        }
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
        voiceRecognitionRepository.finishListening()
        setState { HomeContract.State(voiceState = HomeContract.VoiceState.Idle) }
    }

    private fun changeLanguage(language: Language) {
        setState { HomeContract.State(selectedLanguage = language) }
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
        setState { HomeContract.State(error = null) }
        startVoiceRecognition()
    }

    private fun clearTranscription() {
        setState { HomeContract.State(transcribedText = "", error = null) }
    }

    private fun loadRecentNotes() {
        viewModelScope.launch{
            getNotesUseCase.invoke()
                .collect { notes ->
                    notes.onSuccess {
                        setState { copy(recentNotes = it.take(3).map { note ->  note.copy(timePassed = formatTimeAgo(note.createdAt)) }) }
                    }
                        .onError {

                        }
                }
        }

    }

    private fun formatTimeAgo(createdTimeMillis: Long): String {
        val now = Instant.now()
        val created = Instant.ofEpochMilli(createdTimeMillis)

        val duration = Duration.between(created, now)

        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days == 1L -> "Yesterday"
            days < 7 -> "${days}d"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("d MMM")
                    .withLocale(Locale.getDefault())
                    .withZone(java.time.ZoneId.systemDefault())

                formatter.format(created)
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        voiceRecognitionJob?.cancel()
        voiceRecognitionRepository.finishListening()
    }

    fun getGreeting(): String {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 5..11  -> "Good morning"
            in 12..17 -> "Good afternoon"
            in 18..21 -> "Good evening"
            else      -> "Good night"
        } + ", Aytan" //todo get from prefs
    }

}