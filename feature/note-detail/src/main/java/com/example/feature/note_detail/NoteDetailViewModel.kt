package com.example.feature.note_detail

import androidx.lifecycle.viewModelScope
import com.example.core.common.base.BaseViewModel
import com.example.domain.models.Note
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.usecase.DeleteNoteUseCase
import com.example.domain.usecase.GetNoteUseCase
import com.example.domain.usecase.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private val contentFlow = MutableStateFlow("")
    private val titleFlow = MutableStateFlow("")

    override fun handleIntent(intent: NoteDetailContract.Intent) {
        when (intent) {
            is NoteDetailContract.Intent.LoadNote -> loadNote(intent.id)
            is NoteDetailContract.Intent.DeleteNote -> deleteNote(intent.note)
            is NoteDetailContract.Intent.ChangeText -> updateNote(text = intent.content, title = intent.title)
        }
    }

    private fun updateNote(text: String?, title: String?){
        text?.let{ contentFlow.value = it }
        title?.let{ titleFlow.value = it }
    }

    private fun loadNote(id: Long) {
        setState { NoteDetailContract.State(isLoading = true, error = null) }
        viewModelScope.launch {
            getNoteUseCase.invoke(id)
                .onSuccess {
                    setState { NoteDetailContract.State(isLoading = false, note = it) }

                    contentFlow.value = it.content
                    titleFlow.value = it.title

                    launch{
                        contentFlow
                            .debounce(400)
                            .distinctUntilChanged()
                            .collectLatest { content ->
                                updateNoteUseCase.invoke(note = it, content = content)
                                    .onSuccess {

                                    }.onError {
                                        setState {
                                            NoteDetailContract.State(
                                                isLoading = false,
                                                error = it.message
                                            )
                                        }
                                    }
                            }
                    }

                    launch {
                        titleFlow
                            .debounce(400)
                            .distinctUntilChanged()
                            .collectLatest { content ->
                                updateNoteUseCase.invoke(note = it, title = content)
                                    .onSuccess {

                                    }.onError {
                                        setState {
                                            NoteDetailContract.State(
                                                isLoading = false,
                                                error = it.message
                                            )
                                        }
                                    }
                            }
                    }
                }.onError {
                    setState { NoteDetailContract.State(isLoading = false, error = it.message) }
            }
        }
    }
    private fun deleteNote(note: Note) {
        setState { NoteDetailContract.State(isLoading = true, error = null) }
        viewModelScope.launch {
            deleteNoteUseCase.invoke(note)
                .onSuccess {
                    sendEffect(NoteDetailContract.Effect.NavigateBack)
                }.onError {
                    setState { NoteDetailContract.State(isLoading = false, error = it.message) }
            }
        }
    }

}