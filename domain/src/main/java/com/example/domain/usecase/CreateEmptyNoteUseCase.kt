package com.example.domain.usecase

import com.example.domain.models.DataResult
import com.example.domain.models.Note
import com.example.domain.models.TextBlock
import com.example.domain.models.onSuccess
import com.example.domain.ops.ensureTrailingPlainBlock
import com.example.domain.repository.NotesRepository
import java.util.UUID
import javax.inject.Inject

class CreateEmptyNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(): DataResult<Note> {
        return try {
            var noteId: Long? = null
            val note = Note().copy(textBlocks = emptyList<TextBlock>().ensureTrailingPlainBlock())
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