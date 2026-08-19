package com.aytngr.domain.usecase

import com.aytngr.domain.models.DataResult
import com.aytngr.domain.models.Note
import com.aytngr.domain.models.onSuccess
import com.aytngr.domain.repository.NotesRepository
import com.aytngr.domain.scheduler.ReminderScheduler
import com.aytngr.domain.storage.ImageStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository,
    private val reminderScheduler: ReminderScheduler,
    private val imageStore: ImageStore,
) {
    suspend operator fun invoke(
        note: Note
    ): DataResult<Unit> {
        note.reminderAt?.let{ reminderScheduler.cancel(note.id) }
        val result = notesRepository.deleteNote(note)

        if (result !is DataResult.Success || note.images.isEmpty()) return result

        val stillReferenced = notesRepository.getAllNotes().first()
            .let { if (it is DataResult.Success) it.data else return result }
            .flatMap { it.images }
            .toSet()

        imageStore.delete(note.images.filterNot { it in stillReferenced })
        return result

    }
}