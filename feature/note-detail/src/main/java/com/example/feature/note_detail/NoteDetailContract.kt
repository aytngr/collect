package com.example.feature.note_detail

import androidx.compose.ui.text.input.TextFieldValue
import com.example.core.common.base.BaseEffect
import com.example.core.common.base.BaseIntent
import com.example.core.common.base.BaseState
import com.example.core.ui.RecordingState
import com.example.domain.models.Note

class NoteDetailContract {

    sealed class Intent: BaseIntent {
        data class LoadNote(val id: Long): Intent()
        data class ChangeRecordingState(val recordingState: RecordingState): Intent()
        data class UpdateTextBlock(val textBlockId: String, val field: TextFieldValue): Intent()
        data class FocusBlock(val textBlockId: String) : Intent()
        data class UpdateTitle(val title: String) : Intent()
        data object SaveNow : Intent()
        data class DeleteNote(val note: Note): Intent()
    }

    data class State(
        val note: Note,
        val isLoading: Boolean = false,
        val fieldValues: Map<String, TextFieldValue> = emptyMap(),
        val focusedBlockId: String? = null,
        val recordingState: RecordingState = RecordingState.STOPPED,
        val recordingTime: String = "0:01",
        val error: String? = null,
    ): BaseState

    sealed class Effect: BaseEffect {
        data object NavigateBack: Effect()
    }
}