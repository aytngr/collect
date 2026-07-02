package com.example.domain.usecase

import com.example.domain.models.DataResult
import com.example.domain.models.Note
import com.example.domain.models.onSuccess
import com.example.domain.repository.NotesRepository
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository,
) {
    suspend operator fun invoke(
        title: String?,
        content: String,
        images: List<String?>?,
        reminderAt: Long? = null
    ): DataResult<Note> {
        return try {
            if (content.isBlank()) {
                return DataResult.Error(IllegalArgumentException("Note content cannot be empty"))
            }

            val note = Note()
            var noteId: Long? = null
            notesRepository.insertNote(
                note.copy(
                    title = title ?: "",
                    content = content,
                    images = images,
                    reminderAt = reminderAt,
                    createdAt = System.currentTimeMillis()
                )
            )
                .onSuccess {
                    noteId = it
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