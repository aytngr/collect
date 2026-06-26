package com.example.feature.notes.noteList

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
import com.example.domain.usecase.UpdateNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val updateNotesUseCase: UpdateNotesUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
) : BaseViewModel<NoteListContract.Intent, NoteListContract.State, NoteListContract.Effect>(
    NoteListContract.State()
) {

    init {
        handleIntent(NoteListContract.Intent.LoadNotes)
    }

    override fun handleIntent(intent: NoteListContract.Intent) {
        when (intent) {
            NoteListContract.Intent.ClearSearch -> clearSearch()
            is NoteListContract.Intent.DeleteNote -> deleteNote(intent.note)
            is NoteListContract.Intent.FilterByCategory -> filterByCategory(intent.noteCategory)
            NoteListContract.Intent.LoadNotes -> loadNotes()
            is NoteListContract.Intent.SearchNote -> searchNotes(intent.query)
            NoteListContract.Intent.ClearSelection -> clearSelection()
            NoteListContract.Intent.DeleteSelected -> deleteSelection()
            is NoteListContract.Intent.LongPressNote -> longPressNote(intent.id)
            is NoteListContract.Intent.ToggleSelect -> toggleSelect(intent.id)
            NoteListContract.Intent.SelectAll -> selectAll()
            NoteListContract.Intent.PinSelected -> pinSelected()
            is NoteListContract.Intent.MoveSelected -> moveSelected(intent.category)
            NoteListContract.Intent.CombineSelected -> combineSelected()
        }
    }

    private fun selectAll(){
        setState { copy(selectedIds = selectedIds + notes.map { it.id }) }
    }
    private fun deleteSelection() {
        viewModelScope.launch {
            currentState.notes
                .filter { it.id in currentState.selectedIds }
                .forEach { deleteNoteUseCase(it) }
            sendEffect(NoteListContract.Effect.ShowToast("Notes deleted"))
            clearSelection()
        }
    }

    private fun pinSelected() {
        viewModelScope.launch {
            val selected = currentState.notes.filter { it.id in currentState.selectedIds }
            if (selected.isEmpty()) return@launch
            val pin = selected.any { !it.isPinned }
            updateNotesUseCase(selected.map { it.copy(isPinned = pin) })
            clearSelection()
        }
    }

    private fun moveSelected(category: NoteCategory) {
        val moved = currentState.notes.filter { it.id in currentState.selectedIds }.map { note ->  note.copy(category = category, isCategoryManual = true) }
        viewModelScope.launch {
            updateNotesUseCase(moved)
        }
        clearSelection()
    }

    private fun combineSelected() {
        val selected = currentState.notes.filter { it.id in currentState.selectedIds }
        val content = selected.joinToString("\n\n") { "${it.title}\n${it.content}"  }
        val images = selected.flatMap { it.images.orEmpty() }

        viewModelScope.launch {
            createNoteUseCase("", content, images, Language.AZERBAIJANI)
            selected.forEach { deleteNoteUseCase(it) }
        }

        clearSelection()
    }

    private fun clearSelection() {
        setState { copy(selectionMode = false, selectedIds = emptySet()) }
    }

    private fun longPressNote(id: Long){
        setState {
            copy(selectionMode = true, selectedIds = selectedIds + id)
        }
    }
    private fun toggleSelect(id: Long){
        setState {
            val next = if (id in selectedIds) selectedIds - id else selectedIds + id
            copy(selectedIds = next, selectionMode = next.isNotEmpty())
        }
    }

    private fun loadNotes() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getNotesUseCase.invoke()
                .collect { result ->
                    result.onSuccess { notes ->
                        setState {
                            copy(
                                notes = notes,
                                filteredNotes = applyFilters(notes, selectedCategory, searchQuery),
                                isLoading = false
                            )
                        }
                    }.onError {
                        setState { copy(isLoading = false, error = it.message) }
                    }
                }
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
                filteredNotes = applyFilters(notes, category, searchQuery)
            )
        }
    }

    private fun searchNotes(query: String) {
        setState {
            copy(
                searchQuery = query,
                filteredNotes = applyFilters(notes, selectedCategory, query)
            )
        }
    }

    private fun clearSearch() {
        setState {
            copy(
                searchQuery = "",
                filteredNotes = applyFilters(notes, selectedCategory, "")
            )
        }
    }

    private fun applyFilters(
        notes: List<Note>,
        category: NoteCategory?,
        searchQuery: String
    ): List<Note> {
        var filtered = notes

        if (category != null) {
            filtered = filtered.filter { it.category == category }
        }
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }

        // pinned first, then most-recently edited
        return filtered.sortedWith(
            compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt }
        )
    }
}
