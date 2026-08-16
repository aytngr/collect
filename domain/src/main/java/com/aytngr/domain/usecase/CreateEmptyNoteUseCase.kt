package com.aytngr.domain.usecase

import com.aytngr.domain.models.DataResult
import com.aytngr.domain.models.Note
import com.aytngr.domain.models.onSuccess
import com.aytngr.domain.repository.NotesRepository
import javax.inject.Inject

class CreateEmptyNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(): DataResult<Note> {
        return try {
            var noteId: Long? = null
            val note = Note()
            notesRepository.insertNote(note)
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