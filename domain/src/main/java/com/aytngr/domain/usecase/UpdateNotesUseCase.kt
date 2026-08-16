package com.aytngr.domain.usecase

import com.aytngr.domain.models.DataResult
import com.aytngr.domain.models.Note
import com.aytngr.domain.models.onError
import com.aytngr.domain.models.onSuccess
import com.aytngr.domain.repository.NotesRepository
import javax.inject.Inject

class UpdateNotesUseCase @Inject constructor(
    private val notesRepository: NotesRepository,
) {
    suspend operator fun invoke(
        notes: List<Note>,
    ): DataResult<Unit> {
        return try {
            notesRepository.updateNotes(notes)
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