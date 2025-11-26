package com.example.data.repository

import com.example.data.dao.NoteDao
import com.example.data.extension.handleFlowResponse
import com.example.data.extension.handleResponse
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.models.DataResult
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import com.example.domain.repository.NotesRepository
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

    override suspend fun getNote(id: Long): DataResult<Note> {
        return handleResponse { noteDao.getNote(id).toDomain() }
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

    override suspend fun getNotesByCategory(category: NoteCategory): Flow<DataResult<List<Note>>> {
        return withContext(Dispatchers.IO){
            noteDao.getNotesByCategory(category.name).handleFlowResponse { entities ->
                entities.map { it.toDomain() }
            }
        }
    }
}