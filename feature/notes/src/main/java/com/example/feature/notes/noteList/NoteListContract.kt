package com.example.feature.notes.noteList

import com.example.core.common.base.BaseEffect
import com.example.core.common.base.BaseIntent
import com.example.core.common.base.BaseState
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory

class NoteListContract {
    sealed class Intent: BaseIntent{
        object LoadNotes: Intent()
        data class DeleteNote(val note: Note): Intent()
        data class HandleExpandedNotes(val expandedNote: Note): Intent()
        data class FilterByCategory(val noteCategory: NoteCategory?): Intent()
        data class SearchNote(val query: String): Intent()
        data object ClearSearch: Intent()
    }

    data class State(
        val notes: List<Note> = emptyList<Note>(),
        val filteredNotes: List<Note> = emptyList<Note>(),
        val selectedCategory: NoteCategory? = null,
        val searchQuery: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    ): BaseState

    sealed class Effect: BaseEffect{
        data class ShowToast(val message: String): Effect()
        data class ShowDeleteConfirmation(val noteId: Long): Effect()
    }
}