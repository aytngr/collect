package com.example.domain.usecase

import android.graphics.Bitmap
import com.example.domain.models.DataResult
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.onSuccess
import com.example.domain.processor.NoteProcessor
import com.example.domain.repository.NotesRepository
import dagger.Binds
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository,
    private val noteProcessor: NoteProcessor
) {
    suspend operator fun invoke(
        content: String,
        images: List<String?>?,
        language: Language
    ): DataResult<Note> {
        return try {
            if (content.isBlank()) {
                return DataResult.Error(IllegalArgumentException("Note content cannot be empty"))
            }

            val processedNote = noteProcessor.processVoiceInput(content, language)
                var noteId : Long? = null
            notesRepository.insertNote(processedNote)
                .onSuccess {
                    noteId = it
                }
            noteId?.let {
                val savedNote = processedNote.copy(id = it, images = images, createdAt = System.currentTimeMillis())
                DataResult.Success(savedNote)
            } ?: run {
                DataResult.Error(Exception("Error"))
            }

        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }
}