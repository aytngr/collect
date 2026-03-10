package com.example.feature.notes.noteList

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.example.core.common.base.BaseEffect
import com.example.core.common.base.BaseIntent
import com.example.core.common.base.BaseState
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import kotlinx.coroutines.Job

class NoteListContract {
    sealed class Intent: BaseIntent{
        object LoadNotes: Intent()
        data class DeleteNote(val note: Note): Intent()
        data class CombineNotes(val draggedNote: Note, val targetNote: Note): Intent()
        data class ReorderNotes(val fromNote: Long, val toNote: Long): Intent()
        data class HandleExpandedNotes(val expandedNote: Note): Intent()
        data class FilterByCategory(val noteCategory: NoteCategory?): Intent()
        data class SearchNote(val query: String): Intent()
        data object UpdateNotesWithOrder: Intent()
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

    @Stable
    class DragDropState {
        var draggedId by mutableStateOf<Long?>(null)
        var dropTargetId by mutableStateOf<Long?>(null)

        // Use regular vars for frequently updated values (no recomposition)
        var dragOffsetX = 0f
        private var _dragOffsetY by mutableFloatStateOf(0f)  // Observable!

        var dragOffsetY: Float
            get() = _dragOffsetY
            set(value) { _dragOffsetY = value }  // Triggers recomposition
        var initialItemY = 0f  // NEW: Track initial position

        val itemBounds = mutableMapOf<Long, Pair<Offset, IntSize>>()

        // Auto-scroll
        var autoScrollJob: Job? = null

        fun reset() {
            draggedId = null
            dropTargetId = null
            dragOffsetX = 0f
            dragOffsetY = 0f
            initialItemY = 0f
            autoScrollJob?.cancel()
            autoScrollJob = null
        }

        fun checkCollision() {
            if (draggedId == null) return

            dropTargetId = itemBounds
                .filterKeys { it != draggedId }
                .firstNotNullOfOrNull { (id, pair) ->
                    val (pos, size) = pair
                    if (dragOffsetX in pos.x..(pos.x + size.width) &&
                        dragOffsetY in pos.y..(pos.y + size.height)
                    ) id else null
                }
        }
    }
}