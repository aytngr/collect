package com.example.feature.notes.noteList

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.core.common.base.BaseViewModel
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.usecase.CreateNoteUseCase
import com.example.domain.usecase.DeleteNoteUseCase
import com.example.domain.usecase.GetNotesUseCase
import com.example.domain.usecase.UpdateNoteUseCase
import com.example.domain.usecase.UpdateNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val updateNotesUseCase: UpdateNotesUseCase,
) : BaseViewModel<NoteListContract.Intent, NoteListContract.State, NoteListContract.Effect>(
    NoteListContract.State()
) {

    var noteList by mutableStateOf(emptyList<Note>())
        private set

    var reorderUpdate = false;

    init {
        handleIntent(NoteListContract.Intent.LoadNotes)
    }

    override fun handleIntent(intent: NoteListContract.Intent) {
        when (intent) {
            NoteListContract.Intent.ClearSearch -> clearSearch()
            is NoteListContract.Intent.DeleteNote -> deleteNote(intent.note)
            is NoteListContract.Intent.FilterByCategory -> filterByCategory(intent.noteCategory)
            NoteListContract.Intent.LoadNotes -> loadNotes()
            is NoteListContract.Intent.HandleExpandedNotes -> handleExpandedNotes(intent.expandedNote)
            is NoteListContract.Intent.SearchNote -> searchNotes(intent.query)
            is NoteListContract.Intent.CombineNotes -> combineNotes(intent.draggedNote, intent.targetNote)
            is NoteListContract.Intent.ReorderNotes -> reorderNotes(intent.fromNote, intent.toNote)
            is NoteListContract.Intent.UpdateNotesWithOrder -> onDragEnd()
        }
    }

    private fun combineNotes(draggedNote: Note, targetNote: Note) {
        viewModelScope.launch {
//            val combinedContent = "${targetNote.content}${draggedNote.title}${draggedNote.content}"
            val combinedImages = targetNote.images.orEmpty() + draggedNote.images.orEmpty()

            // Create combined note
//            deleteNoteUseCase(targetNote)
//            createNoteUseCase(title = targetNote.title, combinedContent, combinedImages, Language.AZERBAIJANI)
            //Update target note
//            updateNoteUseCase.invoke(note = targetNote, title = targetNote.title, content = combinedContent, images = combinedImages, language = targetNote.language)
            deleteNoteUseCase(draggedNote)
        }
    }

    private fun reorderNotes(fromNote: Long, toNote: Long) {
        viewModelScope.launch {
            val from = noteList.indexOfFirst { it.id == fromNote }
            val to = noteList.indexOfFirst { it.id == toNote }
            if (from != -1 && to != -1) {
                noteList = noteList.toMutableList().apply { add(to, removeAt(from)) }

                setState {
                    copy(
                        notes = noteList,
                        filteredNotes = applyFilters(
                            noteList,
                            currentState.selectedCategory,
                            currentState.searchQuery
                        )
                    )
                }
            }
        }
    }


    fun onDragEnd() {
        viewModelScope.launch {
            val updated = noteList.mapIndexed { index, note -> note.copy(orderN = index) }
            reorderUpdate = true
            updateNotesUseCase.invoke(updated)
        }
    }

    private fun loadNotes() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getNotesUseCase.invoke()
                .collect { notes ->
                    notes.onSuccess {
                        if(!reorderUpdate){
                            noteList = it.mapIndexed { index, note -> note.copy(orderN = index)}
                            setState {
                                copy(
                                    notes = it,
                                    filteredNotes = applyFilters(
                                        it,
                                        category = currentState.selectedCategory,
                                        searchQuery = currentState.searchQuery
                                    ),
                                    isLoading = false
                                )
                            }
                        }
                        reorderUpdate = false
                    }.onError {
                        setState { copy(isLoading = false, error = it.message) }
                    }
                }
        }
    }

    private fun handleExpandedNotes(expandedNote: Note) {
        setState {
            copy(filteredNotes = currentState.filteredNotes.map {
                if (it.id == expandedNote.id) it.copy(isRevealed = true) else it.copy(isRevealed = false)
            })
        }
    }

    private fun deleteNote(note: Note) {
        viewModelScope.launch {
            deleteNoteUseCase(note).onSuccess {
                sendEffect(NoteListContract.Effect.ShowToast("Note deleted"))
            }.onError {
                setState { copy(error = it.message ?: "Failed to delete note") }
            }
        }
    }

    private fun filterByCategory(category: NoteCategory?) {
        setState {
            copy(
                selectedCategory = category,
                filteredNotes = applyFilters(currentState.notes, category, currentState.searchQuery)
            )
        }
    }

    private fun searchNotes(query: String) {
        setState {
            copy(
                searchQuery = query,
                filteredNotes = applyFilters(
                    currentState.notes,
                    currentState.selectedCategory,
                    query
                )
            )
        }
    }

    private fun clearSearch() {
        setState {
            copy(
                searchQuery = "",
                filteredNotes = applyFilters(currentState.notes, currentState.selectedCategory, "")
            )
        }
    }

    private fun applyFilters(
        notes: List<Note>,
        category: NoteCategory?,
        searchQuery: String
    ): List<Note> {
        var filtered = notes

        // Filter by category
        if (category != null) {
            filtered = filtered.filter { it.category == category }
        }

        // Filter by search query
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
        }

        return filtered
    }

}