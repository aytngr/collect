package com.example.domain.usecase

import com.example.domain.models.DataResult
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.TextBlock
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.repository.NotesRepository
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository,
) {
    suspend operator fun invoke(
        note: Note,
        textBlocks: List<TextBlock>? = null,
        title: String? = null,
        images: List<String?>? = null,
        language: Language? = null
    ): DataResult<Unit> {
        return try {
            val updatedNote = note.copy(
                textBlocks = textBlocks ?: note.textBlocks,
                title = title ?: note.title,
                images = images ?: note.images
            )

            notesRepository.updateNote(updatedNote)
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