package com.example.feature.notes.noteDetail

import com.example.core.common.base.BaseEffect
import com.example.core.common.base.BaseIntent
import com.example.core.common.base.BaseState
import com.example.domain.models.Note

class NoteDetailContract {

    sealed class Intent: BaseIntent {
        data class LoadNote(val id: Long): Intent()
        data class ChangeText(val note: Note, val content: String? = null, val title: String? = null): Intent()
        data class DeleteNote(val note: Note): Intent()
    }

    data class State(
        val note: Note? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
    ): BaseState

    sealed class Effect: BaseEffect {
        data object NavigateBack: Effect()
    }
}