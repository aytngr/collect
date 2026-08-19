package com.aytngr.domain.usecase

import com.aytngr.domain.models.DataResult
import com.aytngr.domain.models.Note
import com.aytngr.domain.models.onSuccess
import com.aytngr.domain.repository.NotesRepository
import com.aytngr.domain.scheduler.ReminderScheduler
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke(
        title: String?,
        content: String,
        images: List<String>,
        reminderAt: Long? = null
    ): DataResult<Note> {
        return try {
            if (content.isBlank()) {
                return DataResult.Error(IllegalArgumentException("Note content cannot be empty"))
            }

            val note = Note(
                title = title ?: "",
                content = content,
                images = images,
                reminderAt = reminderAt,
                createdAt = System.currentTimeMillis()
            )
            var noteId: Long? = null
            notesRepository.insertNote(note)
                .onSuccess {
                    noteId = it
                    reminderAt?.let{ reminder -> reminderScheduler.schedule(it, null, reminder) }
                }
            noteId?.let {
                val savedNote = note.copy(id = it)
                DataResult.Success(savedNote)
            } ?: run {
                DataResult.Error(Exception("Error"))
            }

        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }
}