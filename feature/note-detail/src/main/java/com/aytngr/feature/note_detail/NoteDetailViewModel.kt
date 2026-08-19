package com.aytngr.feature.note_detail

import android.os.Build
import androidx.lifecycle.viewModelScope
import com.aytngr.core.common.base.BaseViewModel
import com.aytngr.core.common.base.permissions.PermissionHandler
import com.aytngr.domain.models.Note
import com.aytngr.domain.models.onError
import com.aytngr.domain.models.onSuccess
import com.aytngr.domain.scheduler.ReminderScheduler
import com.aytngr.domain.usecase.DeleteNoteUseCase
import com.aytngr.domain.usecase.GetNoteUseCase
import com.aytngr.domain.usecase.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val getNoteUseCase: GetNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val permissionHandler: PermissionHandler,
    private val reminderScheduler: ReminderScheduler
) : BaseViewModel<NoteDetailContract.Intent, NoteDetailContract.State, NoteDetailContract.Effect>(
    NoteDetailContract.State(
        note = Note()
    )
) {
    private var saveJob: Job? = null

    private var pendingReminder: Long? = null

    override fun handleIntent(intent: NoteDetailContract.Intent) {
        when (intent) {
            is NoteDetailContract.Intent.LoadNote -> loadNote(intent.id)
            is NoteDetailContract.Intent.DeleteNote -> deleteNote(intent.note)
            is NoteDetailContract.Intent.UpdateNote -> updateNote(
                title = intent.title,
                content = intent.content,
                images = intent.images
            )

            NoteDetailContract.Intent.SaveNow -> persist()
            is NoteDetailContract.Intent.SetReminder -> setReminder(intent.at)
            is NoteDetailContract.Intent.SetPendingReminder -> pendingReminder = intent.at
            is NoteDetailContract.Intent.SchedulePendingReminder -> schedulePendingReminder()
            NoteDetailContract.Intent.ClearReminder -> clearReminder()
            NoteDetailContract.Intent.PinNote -> pinNote()
            NoteDetailContract.Intent.CheckExactAlarmPermission -> checkExactAlarmPermission()
            NoteDetailContract.Intent.RefreshPermissionDialogVisibility -> {
                setState { copy(showExactAlarmPermissionDialog = false) }
            }
        }
    }

    private fun schedulePendingReminder() {
        pendingReminder?.let {
            setReminder(it)
            pendingReminder = null
        }
    }
    private fun checkExactAlarmPermission() {
        if (!reminderScheduler.canExact()) {
            setState { copy(showExactAlarmPermissionDialog = true) }
        }else {
            schedulePendingReminder()
        }
    }

    private fun setReminder(at: Long) {
        setState { copy(note = note.copy(reminderAt = at)) }
        persist()
        reminderScheduler.schedule(currentState.note.id, currentState.note.title, at)
    }

    private fun clearReminder() {
        setState { copy(note = note.copy(reminderAt = null)) }
        persist()
        reminderScheduler.cancel(currentState.note.id)
    }

    private fun pinNote() {
        setState { copy(note = note.copy(isPinned = !note.isPinned)) }
        persist()
    }

    private fun updateNote(title: String?, content: String?, images: List<String>?) {
        setState {
            copy(
                note = note.copy(
                    title = title ?: note.title,
                    content = content ?: note.content,
                    images = images ?: note.images,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            persist()
        }
    }

    private fun persist() {
        viewModelScope.launch {
            updateNoteUseCase.invoke(currentState.note)
        }
    }

    private fun loadNote(id: Long) {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getNoteUseCase.invoke(id)
                .onSuccess {
                    it?.let{
                        setState {
                            copy(
                                isLoading = false,
                                note = it,
                            )
                        }
                    } ?: run { setState { copy(isLoading = false, error = "Not found") } }
                }.onError {
                    setState { copy(isLoading = false, error = it.message) }
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
                    setState { copy(isLoading = false, error = it.message) }
                }
        }
    }


}