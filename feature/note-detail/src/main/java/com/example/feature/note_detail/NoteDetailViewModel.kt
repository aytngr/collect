package com.example.feature.note_detail

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.example.core.common.base.BaseViewModel
import com.example.core.common.base.permissions.PermissionHandler
import com.example.core.ui.RecordingState
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.TextBlock
import com.example.domain.models.VoiceRecognitionResult
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.ops.ensureTrailingPlainBlock
import com.example.domain.ops.insertInlineVoiceText
import com.example.domain.ops.insertVoiceBlock
import com.example.domain.repository.VoiceRecognitionRepository
import com.example.domain.usecase.DeleteNoteUseCase
import com.example.domain.usecase.GetNoteUseCase
import com.example.domain.usecase.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val getNoteUseCase: GetNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val permissionHandler: PermissionHandler,
    private val voiceRecognitionRepository: VoiceRecognitionRepository
) : BaseViewModel<NoteDetailContract.Intent, NoteDetailContract.State, NoteDetailContract.Effect>(
    NoteDetailContract.State(
        note = Note()
    )
) {

    private var voiceRecognitionJob: Job? = null
    private var saveJob: Job? = null

    override fun handleIntent(intent: NoteDetailContract.Intent) {
        when (intent) {
            is NoteDetailContract.Intent.LoadNote -> loadNote(intent.id)
            is NoteDetailContract.Intent.DeleteNote -> deleteNote(intent.note)
            is NoteDetailContract.Intent.UpdateTextBlock -> updateTextBlock(
                textBlockId = intent.textBlockId,
                field = intent.field
            )

            is NoteDetailContract.Intent.ChangeRecordingState -> changeRecordingState(intent.recordingState)
            is NoteDetailContract.Intent.FocusBlock -> focusBlock(intent.textBlockId)
            NoteDetailContract.Intent.SaveNow -> persist()
            is NoteDetailContract.Intent.UpdateTitle -> updateTitle(title = intent.title)
        }
    }

    private fun updateTitle(title: String) {
        setState { copy(note = note.copy(title = title)) }
        scheduleAutoSave()
    }

    private fun focusBlock(textBlockId: String) {
        setState { copy(focusedBlockId = textBlockId) }
    }

    private fun startListeningForContent() {
        voiceRecognitionJob?.cancel()
        voiceRecognitionJob = voiceRecognitionRepository
            .startListening(Language.AZERBAIJANI)
            .onEach { result ->
                when (result) {
                    is VoiceRecognitionResult.Success -> {
                        changeRecordingState(RecordingState.STOPPED)
                        processNoteContent(
                            text = result.text,
                            audioPath = "",
                            durationMs = 0
                        )
                    }

                    is VoiceRecognitionResult.Error -> {
                        changeRecordingState(RecordingState.STOPPED)
                    }

                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    private fun processNoteContent(text: String, audioPath: String, durationMs: Long) {
        viewModelScope.launch {
            val focusedId = currentState.focusedBlockId ?: currentState.note.textBlocks.last().id
            val textBlocks = currentState.note.textBlocks

//            val newTextBlocks =
            if (true/*preferences.inlineVoice*/) {
                val cursor =
                    currentState.fieldValues[focusedId]?.selection?.start ?: findTextBlockLength(
                        textBlocks,
                        focusedId
                    )
                val (newTextBlocks, mergedText, newTextLength) =
                    insertInlineVoiceText(
                        blocks = textBlocks,
                        focusedId = focusedId,
                        text = text,
                        cursor = cursor
                    )

                setState {
                    copy(
                        note = note.copy(textBlocks = newTextBlocks.ensureTrailingPlainBlock()),
                        fieldValues = fieldValues + (focusedId to TextFieldValue(
                            text = mergedText,
                            TextRange(cursor + newTextLength)
                        ))
                    )
                }
            } else {
                val newTextBlocks = insertVoiceBlock(textBlocks, focusedId, text, audioPath, durationMs)

                setState {
                    copy(
                        note = note.copy(textBlocks = newTextBlocks.ensureTrailingPlainBlock()),
                    )
                }
            }
            scheduleAutoSave()
        }
    }

    private fun changeRecordingState(state: RecordingState) {
        when (state) {
            RecordingState.PAUSED -> setState { copy(recordingState = RecordingState.PAUSED) }
            RecordingState.STOPPED -> {
                voiceRecognitionRepository.stopListening()
                setState { copy(recordingState = RecordingState.STOPPED) }
            }

            RecordingState.RESUMED -> setState { copy(recordingState = RecordingState.RESUMED) }
            RecordingState.PROCESSING -> setState { copy(recordingState = RecordingState.PROCESSING) }
            RecordingState.RECORDING -> {
                setState { copy(recordingState = RecordingState.RECORDING) }
                startListeningForContent()
            }
        }
    }

    private fun updateTextBlock(textBlockId: String, field: TextFieldValue) {
        setState {
            copy(note = note.copy(textBlocks = note.textBlocks.map { block ->
                when {
                    block.id == textBlockId && block is TextBlock.PlainText ->
                        block.copy(text = field.text)

                    block.id == textBlockId && block is TextBlock.VoiceText ->
                        block.copy(text = field.text)

                    else -> block
                }
            }), fieldValues = fieldValues + (textBlockId to field))
        }

        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            persist()
        }
    }

    private fun persist() {
        viewModelScope.launch {
            updateNoteUseCase.invoke(currentState.note)
        }
    }

    private fun loadNote(id: Long) {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getNoteUseCase.invoke(id)
                .onSuccess {
                    setState { copy(isLoading = false, note = it) }
                }.onError {
                    setState { copy(isLoading = false, error = it.message) }
                }
        }
    }

    private fun findTextBlockLength(textBlocks: List<TextBlock>, focusedId: String): Int {
        val block = textBlocks.find { block -> block.id == focusedId }
        return when (block) {
            is TextBlock.PlainText -> block.text.length
            is TextBlock.VoiceText -> block.text.length
            null -> 0
        } - 1
    }

    private fun deleteNote(note: Note) {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            deleteNoteUseCase.invoke(note)
                .onSuccess {
                    sendEffect(NoteDetailContract.Effect.NavigateBack)
                }.onError {
                    setState { copy(isLoading = false, error = it.message) }
                }
        }
    }

}