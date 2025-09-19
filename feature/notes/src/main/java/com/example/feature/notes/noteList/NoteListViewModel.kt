package com.example.feature.notes.noteList

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.common.base.BaseViewModel
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.usecase.DeleteNoteUseCase
import com.example.domain.usecase.GetNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
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
            is NoteListContract.Intent.HandleExpandedNotes -> handleExpandedNotes(intent.expandedNote)
            is NoteListContract.Intent.SearchNote -> searchNotes(intent.query)
        }
    }

    private fun loadNotes() {
        setState { copy(isLoading = true, error = null) }
        getNotesUseCase.invoke()
            .onEach { notes ->
                notes.onSuccess {
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
                }.onError {
                    setState { copy(isLoading = false, error = it.message) }
                }
            }
            .launchIn(viewModelScope)
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
                it.content.contains(searchQuery, ignoreCase = true)
            }
        }

        return filtered
    }

}