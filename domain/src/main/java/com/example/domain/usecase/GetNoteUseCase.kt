package com.example.domain.usecase

import com.example.domain.models.DataResult
import com.example.domain.models.Note
import com.example.domain.repository.NotesRepository
import javax.inject.Inject

class GetNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(id: Long): DataResult<Note> {
        return notesRepository.getNote(id)
    }
}