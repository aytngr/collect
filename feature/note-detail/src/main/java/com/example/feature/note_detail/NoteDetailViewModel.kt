package com.example.feature.note_detail

import android.os.Build
import androidx.lifecycle.viewModelScope
import com.example.core.common.base.BaseViewModel
import com.example.core.common.base.formatCreatedAt
import com.example.core.common.base.permissions.PermissionHandler
import com.example.domain.models.Note
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.scheduler.ReminderScheduler
import com.example.domain.usecase.DeleteNoteUseCase
import com.example.domain.usecase.GetNoteUseCase
import com.example.domain.usecase.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
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
        }
    }
    private fun checkExactAlarmPermission() {
        if (!reminderScheduler.canExact()) {
            setState { copy(showExactAlarmPermissionDialog = true) }
        }
    }

    private fun setReminder(at: Long) {
        setState { copy(note = note.copy(reminderAt = at), reminder = at.formatCreatedAt()) }
        persist()
        reminderScheduler.schedule(currentState.note.id, currentState.note.title, at)
    }

    private fun clearReminder() {
        setState { copy(note = note.copy(reminderAt = null), reminder = "") }
        persist()
        reminderScheduler.cancel(currentState.note.id)
    }

    private fun pinNote() {
        setState { copy(note = note.copy(isPinned = !note.isPinned)) }
    }

    private fun updateNote(title: String?, content: String?, images: List<String>?) {
        setState {
            copy(
                note = note.copy(
                    title = title ?: note.title,
                    content = content ?: note.content,
                    images = images ?: note.images,
                    updatedAt = System.currentTimeMillis()
                ),
                editedTime = formatEditedTime(System.currentTimeMillis())
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
                    setState {
                        copy(
                            isLoading = false,
                            note = it,
                            createdTime = it.createdAt.formatCreatedAt(),
                            editedTime = formatEditedTime(it.updatedAt),
                            reminder = it.reminderAt?.formatCreatedAt() ?: run { "" }
                        )
                    }
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

    private fun formatEditedTime(createdTimeMillis: Long): String {
        val now = Instant.now()
        val created = Instant.ofEpochMilli(createdTimeMillis)

        val duration = Duration.between(created, now)

        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()

        return when {
            minutes < 1 -> "Edited just now"
            minutes < 60 -> "Edited ${minutes}m ago"
            hours < 24 -> "Edited${hours}h ago"
            days == 1L -> " Edited yesterday"
            days < 7 -> "Edited ${days}d ago"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("d MMM")
                    .withLocale(Locale.getDefault())
                    .withZone(java.time.ZoneId.systemDefault())

                "Edited on ${formatter.format(created)}"
            }
        }

    }

}