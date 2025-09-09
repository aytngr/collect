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
import kotlinx.coroutines.flow.Flow

class NotesRepositoryImpl(
    private val noteDao: NoteDao
) : NotesRepository {
    override fun getAllNotes(): Flow<DataResult<List<Note>>> {
        return noteDao.getNotes().handleFlowResponse { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertNote(note: Note) {
        handleResponse { noteDao.insertNote(note.toEntity()) }
    }

    override suspend fun deleteNote(note: Note) {
        handleResponse { noteDao.deleteNote(note.toEntity()) }
    }

    override suspend fun updateNote(note: Note){
        handleResponse { noteDao.updateNote(note.toEntity()) }
    }

    override fun getNotesByCategory(category: NoteCategory): Flow<DataResult<List<Note>>> {
        return noteDao.getNotesByCategory(category.name).handleFlowResponse { entities ->
            entities.map { it.toDomain() }
        }
    }
}