package com.example.feature.notes.noteList

import androidx.annotation.StringRes
import com.example.core.common.base.BaseEffect
import com.example.core.common.base.BaseIntent
import com.example.core.common.base.BaseState
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory

class NoteListContract {
    sealed class Intent: BaseIntent {
        object LoadNotes: Intent()
        object SelectAll: Intent()
        data class DeleteNote(val note: Note): Intent()
        data class FilterByCategory(val noteCategory: NoteCategory?): Intent()
        data class SearchNote(val query: String): Intent()

        data class LongPressNote(val id: Long): Intent()
        data class ToggleSelect(val id: Long): Intent()
        data object ClearSelection: Intent()
        data object DeleteSelected: Intent()
        data object PinSelected: Intent()
        data class MoveSelected(val category: NoteCategory): Intent()
        data object CombineSelected: Intent()
        data object ClearSearch: Intent()
    }

    data class State(
        val notes: List<Note> = emptyList(),
        val filteredNotes: List<Note> = emptyList(),
        val selectedCategory: NoteCategory? = null,
        val searchQuery: String = "",
        val selectionMode: Boolean = false,
        val selectedIds: Set<Long> = emptySet(),
        val isLoading: Boolean = false,
        val error: String? = null
    ): BaseState

    sealed class Effect: BaseEffect {
        data class ShowToast(@StringRes val messageRes: Int): Effect()
        data class ShowDeleteConfirmation(val noteId: Long): Effect()
    }
}
