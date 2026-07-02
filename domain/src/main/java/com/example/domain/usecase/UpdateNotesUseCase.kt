package com.example.domain.usecase

import com.example.domain.models.DataResult
import com.example.domain.models.Note
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.repository.NotesRepository
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