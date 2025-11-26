package com.example.feature.notes.noteDetail

import androidx.lifecycle.viewModelScope
import com.example.core.common.base.BaseViewModel
import com.example.domain.models.Note
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.usecase.DeleteNoteUseCase
import com.example.domain.usecase.GetNoteUseCase
import com.example.domain.usecase.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val getNoteUseCase: GetNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : BaseViewModel<NoteDetailContract.Intent, NoteDetailContract.State, NoteDetailContract.Effect>(
    NoteDetailContract.State()
) {

    override fun handleIntent(intent: NoteDetailContract.Intent) {
        when (intent) {
            is NoteDetailContract.Intent.LoadNote -> loadNote(intent.id)
            is NoteDetailContract.Intent.DeleteNote -> deleteNote(intent.note)
            is NoteDetailContract.Intent.ChangeText -> updateNote(note = intent.note, text = intent.text)
        }
    }

    private fun updateNote(note:Note, text: String){
        viewModelScope.launch {
            updateNoteUseCase.invoke(note = note, content = text)
                .onSuccess {
//                    loadNote(note.id)
                }.onError {
                    setState { copy(isLoading = false, error = it.message)  }
                }
        }
    }

    private fun loadNote(id: Long) {
//        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getNoteUseCase.invoke(id)
                .onSuccess {
                    setState { copy(isLoading = false, note = it) }
                }.onError {
                    setState { copy(isLoading = false, error = it.message)  }
            }
        }
    }
    private fun deleteNote(note: Note) {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            deleteNoteUseCase.invoke(note)
                .onSuccess {
                    sendEffect(NoteDetailContract.Effect.NavigateBack)
                }.onError {
                    setState { copy(isLoading = false, error = it.message)  }
            }
        }
    }

}