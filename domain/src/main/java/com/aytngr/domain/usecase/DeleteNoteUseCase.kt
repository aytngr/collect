package com.aytngr.domain.usecase

import com.aytngr.domain.models.DataResult
import com.aytngr.domain.models.Note
import com.aytngr.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(
        note: Note
    ): DataResult<Unit> {
        return notesRepository.deleteNote(note)
    }
}