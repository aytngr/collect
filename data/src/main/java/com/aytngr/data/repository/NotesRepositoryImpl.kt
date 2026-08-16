package com.aytngr.data.repository

import com.aytngr.data.dao.NoteDao
import com.aytngr.data.extension.handleFlowResponse
import com.aytngr.data.extension.handleResponse
import com.aytngr.data.mapper.toDomain
import com.aytngr.data.mapper.toEntity
import com.aytngr.domain.models.DataResult
import com.aytngr.domain.models.Note
import com.aytngr.domain.models.NoteCategory
import com.aytngr.domain.repository.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NotesRepository {
    override suspend fun getAllNotes(): Flow<DataResult<List<Note>>> {
        return withContext(Dispatchers.IO){
            noteDao.getNotes().handleFlowResponse { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun getNote(id: Long): DataResult<Note?> {
        return handleResponse { noteDao.getNote(id)?.toDomain() }
    }

    override suspend fun insertNote(note: Note): DataResult<Long> {
        return handleResponse { noteDao.insertNote(note.toEntity()) }
    }

    override suspend fun deleteNote(note: Note): DataResult<Unit> {
        return handleResponse { noteDao.deleteNote(note.toEntity()) }
    }

    override suspend fun updateNote(note: Note): DataResult<Unit> {
        return handleResponse { noteDao.updateNote(note.toEntity()) }
    }

    override suspend fun updateNotes(notes: List<Note>): DataResult<Unit> {
        return handleResponse { noteDao.updateNotes(notes.map { it.toEntity() }) }
    }

    override suspend fun getNotesByCategory(category: NoteCategory): Flow<DataResult<List<Note>>> {
        return withContext(Dispatchers.IO){
            noteDao.getNotesByCategory(category.name).handleFlowResponse { entities ->
                entities.map { it.toDomain() }
            }
        }
    }
}