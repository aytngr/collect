package com.example.domain.usecase

import com.example.domain.models.DataResult
import com.example.domain.models.Note
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.repository.NotesRepository
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