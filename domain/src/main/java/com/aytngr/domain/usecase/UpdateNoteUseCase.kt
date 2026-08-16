package com.aytngr.domain.usecase

import com.aytngr.domain.models.DataResult
import com.aytngr.domain.models.Note
import com.aytngr.domain.models.onError
import com.aytngr.domain.models.onSuccess
import com.aytngr.domain.repository.NotesRepository
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository,
) {
    suspend operator fun invoke(
        note: Note,
    ): DataResult<Unit> {
        return try {
            notesRepository.updateNote(note)
                .onSuccess {
                    DataResult.Success(it)
                }
                .onError {
                    DataResult.Error(it)
                }
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }
}